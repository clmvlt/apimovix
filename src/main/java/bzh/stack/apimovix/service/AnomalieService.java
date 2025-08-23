package bzh.stack.apimovix.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.anomalie.AnomalieCreateDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieSearchDTO;
import bzh.stack.apimovix.mapper.AnomalieMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Picture.AnomaliePicture;
import bzh.stack.apimovix.model.StatusType.TypeAnomalie;
import bzh.stack.apimovix.repository.anomalie.AnomaliePictureRepository;
import bzh.stack.apimovix.repository.anomalie.AnomalieRepository;
import bzh.stack.apimovix.repository.anomalie.TypeAnomalieRepository;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
import bzh.stack.apimovix.service.picture.PictureService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnomalieService {

    private final AnomaliePictureRepository anomaliePictureRepository;
    private final AnomalieMapper anomalieMapper;
    private final AnomalieRepository anomalieRepository;
    private final TypeAnomalieRepository typeAnomalieRepository;
    private final PharmacyService pharmacyService;
    private final PackageService packageService;
    private final PictureService pictureService;
    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;

    @Transactional(readOnly = true)
    public List<Anomalie> findAnomalies(Account account) {
        return anomalieRepository.findAnomalies(account);
    }

    @Transactional(readOnly = true)
    public Optional<Anomalie> findAnomalie(Account account, UUID id) {
        Anomalie anomalie = anomalieRepository.findAnomalie(account, id);
        if (anomalie == null) {
            return Optional.empty();
        }
        
        List<AnomaliePicture> pictures = anomaliePictureRepository.findAnomaliePictureByIdAndAccount(account, id);
        anomalie.setPictures(pictures);

        return Optional.of(anomalie);
    }

    @Transactional(readOnly = true)
    public List<Anomalie> searchAnomalies(Account account, AnomalieSearchDTO searchDTO) {
        // Vérifier si des dates sont fournies
        boolean hasDateDebut = searchDTO.getDateDebut() != null && !searchDTO.getDateDebut().isEmpty();
        boolean hasDateFin = searchDTO.getDateFin() != null && !searchDTO.getDateFin().isEmpty();
        
        if (hasDateDebut || hasDateFin) {
            // Conversion des dates
            LocalDateTime dateDebut = null;
            LocalDateTime dateFin = null;
            
            if (hasDateDebut) {
                LocalDate debut = LocalDate.parse(searchDTO.getDateDebut());
                dateDebut = debut.atStartOfDay();
            } else {
                // Si pas de date de début, utiliser une date très ancienne
                dateDebut = LocalDateTime.of(1900, 1, 1, 0, 0);
            }
            
            if (hasDateFin) {
                LocalDate fin = LocalDate.parse(searchDTO.getDateFin());
                dateFin = fin.atTime(LocalTime.MAX); // Fin de la journée
            } else {
                // Si pas de date de fin, utiliser une date très future
                dateFin = LocalDateTime.of(2100, 12, 31, 23, 59, 59);
            }
            
            return anomalieRepository.searchAnomaliesWithDateRange(
                account,
                searchDTO.getUserId(),
                dateDebut,
                dateFin,
                searchDTO.getCip(),
                searchDTO.getTypeCode()
            );
        } else {
            // Pas de filtres de date
            return anomalieRepository.searchAnomalies(
                account,
                searchDTO.getUserId(),
                searchDTO.getCip(),
                searchDTO.getTypeCode()
            );
        }
    }

    @Transactional
    public Optional<Anomalie> createAnomalie(Profil profil, AnomalieCreateDTO anomalieCreateDTO) {
        Optional<TypeAnomalie> typeAnomalieOptional = typeAnomalieRepository.findTypeAnomalie(anomalieCreateDTO.getCode());
        if (typeAnomalieOptional.isEmpty()) {
            return Optional.empty();
        }
        Optional<Pharmacy> pharmacyOptional = pharmacyService.findPharmacy(anomalieCreateDTO.getCip());
        if (pharmacyOptional.isEmpty()) {
            return Optional.empty();
        }

        List<PackageEntity> packages = packageService.findPackagesByBarcodes(profil.getAccount(), anomalieCreateDTO.getBarcodes());

        Anomalie anomalie = new Anomalie();
        anomalieMapper.updateAnomalieFromCreateDTO(anomalieCreateDTO, anomalie);
        anomalie.setId(UUID.randomUUID());
        anomalie.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Paris")));
        anomalie.setAccount(profil.getAccount());
        anomalie.setProfil(profil);
        anomalie.setTypeAnomalie(typeAnomalieOptional.get());
        anomalie.setPharmacy(pharmacyOptional.get());

        anomalie.getPackages().addAll(packages);

        final Anomalie savedAnomalie = anomalieRepository.save(anomalie);

        List<AnomaliePicture> pictures = anomalieCreateDTO.getPictures().stream()
            .map(picture -> createAnomaliePicture(savedAnomalie, picture.getBase64()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        anomaliePictureRepository.saveAll(pictures);
        
        savedAnomalie.setPictures(pictures);
        
        final Anomalie finalAnomalie = anomalieRepository.save(savedAnomalie);
        
        // Envoyer l'email de notification de manière asynchrone
        sendAnomalieEmailAsync(finalAnomalie);
        
        return Optional.of(finalAnomalie);
    }

    /**
     * Envoie l'email d'anomalie de manière asynchrone pour ne pas bloquer la transaction
     */
    @Async("taskExecutor")
    public void sendAnomalieEmailAsync(Anomalie anomalie) {
        try {
            byte[] pdfBytes = pdfGeneratorService.generateAnomaliePdf(anomalie);
            emailService.sendAnomalieNotificationEmail(anomalie, pdfBytes);
        } catch (Exception e) {
            // Erreur silencieuse pour ne pas bloquer la création de l'anomalie
        }
    }

    @Transactional
    public Optional<AnomaliePicture> createAnomaliePicture(Anomalie anomalie, String base64Image) {
        String fileName = pictureService.saveAnomalieImage(anomalie, base64Image);
        if (fileName == null) {
            return Optional.empty();
        }

        try {
            AnomaliePicture picture = new AnomaliePicture();
            picture.setName(fileName);
            picture.setAnomalie(anomalie);
            return Optional.of(picture);
        } catch (Exception e) {
            pictureService.deleteImage(fileName);
            return Optional.empty();
        }
    }
} 