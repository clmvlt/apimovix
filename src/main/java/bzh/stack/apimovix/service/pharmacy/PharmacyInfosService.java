package bzh.stack.apimovix.service.pharmacy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.dto.pharmacyinfos.PharmacyInfosCreateDTO;
import bzh.stack.apimovix.mapper.PharmacyInfosMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.PharmacyInfos;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;
import bzh.stack.apimovix.model.Notification.NotificationType;
import bzh.stack.apimovix.repository.pharmacy.PharmacyInfosPictureRepository;
import bzh.stack.apimovix.repository.pharmacy.PharmacyInfosRepository;
import bzh.stack.apimovix.service.NotificationService;
import bzh.stack.apimovix.service.picture.PictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PharmacyInfosService {

    private final PharmacyInfosRepository pharmacyInfosRepository;
    private final PharmacyInfosPictureRepository pharmacyInfosPictureRepository;
    private final PharmacyInfosMapper pharmacyInfosMapper;
    private final PharmacyService pharmacyService;
    private final NotificationService notificationService;
    private final PictureService pictureService;

    @Transactional(readOnly = true)
    public List<PharmacyInfos> findPharmaciesInfos(Account account) {
        return pharmacyInfosRepository.findPharmaciesInfos(account);
    }

    @Transactional(readOnly = true)
    public Optional<PharmacyInfos> findPharmacyInfos(UUID id, Account account) {
        PharmacyInfos pharmacyInfos = pharmacyInfosRepository.findPharmacyInfos(id, account);
        if (pharmacyInfos == null) {
            return Optional.empty();
        }
        return Optional.of(pharmacyInfos);
    }

    @Transactional
    public Optional<PharmacyInfos> createPharmacyInfos(Profil profil, PharmacyInfosCreateDTO pharmacyInfosCreateDTO) {
        Optional<Pharmacy> optPharmacy = pharmacyService.findPharmacy(pharmacyInfosCreateDTO.getCip());
        if (optPharmacy.isEmpty()) {
            return Optional.empty();
        }
        Pharmacy pharmacy = optPharmacy.get();

        PharmacyInfos pharmacyInfos = new PharmacyInfos();
        pharmacyInfosMapper.updateEntityFromCreateDto(pharmacyInfosCreateDTO, pharmacyInfos);
        pharmacyInfos.setId(UUID.randomUUID());
        pharmacyInfos.setPharmacy(pharmacy);
        pharmacyInfos.setProfil(profil);
        pharmacyInfos.setAccount(profil.getAccount());

        final PharmacyInfos savedPharmacyInfos = pharmacyInfosRepository.save(pharmacyInfos);

        List<PharmacyInfosPicture> pharmacyInfosPictures = pharmacyInfosCreateDTO.getPictures().stream()
            .map(picture -> createPharmacyInfosPicture(picture, savedPharmacyInfos))
            .toList();
        
        pharmacyInfosPictureRepository.saveAll(pharmacyInfosPictures);

        savedPharmacyInfos.getPictures().clear();
        savedPharmacyInfos.getPictures().addAll(pharmacyInfosPictures);

        // Créer une notification pour l'account
        try {
            String notificationTitle = "Nouvelle information pharmacie ajoutée";
            String notificationMessage = String.format("Une nouvelle information a été ajoutée pour la pharmacie %s",
                    pharmacy.getName());
            notificationService.sendNotificationWithEntity(
                    profil.getAccount().getId(),
                    NotificationType.INFORMATION,
                    notificationTitle,
                    notificationMessage,
                    "PHARMACY_INFO",
                    savedPharmacyInfos.getId()
            );
        } catch (Exception e) {
            log.error("Error creating notification for pharmacy info: {}", e.getMessage(), e);
        }

        return Optional.of(savedPharmacyInfos);
    }

    @Transactional
    public PharmacyInfosPicture createPharmacyInfosPicture(PictureDTO picture, PharmacyInfos pharmacyInfos) {
        String fileName = pictureService.savePharmacyInfosImage(pharmacyInfos, picture.getBase64());
        PharmacyInfosPicture pharmacyInfosPicture = new PharmacyInfosPicture();
        pharmacyInfosPicture.setName(fileName);
        pharmacyInfosPicture.setPharmacyInfos(pharmacyInfos);
        return pharmacyInfosPicture;
    }

    @Transactional
    public void deletePharmacyInfos(UUID id, Account account) {
        Optional<PharmacyInfos> optPharmacyInfos = findPharmacyInfos(id, account);
        if (optPharmacyInfos.isPresent()) {
            PharmacyInfos pharmacyInfos = optPharmacyInfos.get();
            
            pharmacyInfos.getPictures().forEach(picture -> {
                String filepath = "pharmacy-infos/" + pharmacyInfos.getId() + "/" + picture.getName();
                pictureService.deleteImage(filepath);
            });
            
            pharmacyInfosPictureRepository.deleteAll(pharmacyInfos.getPictures());
            
            pharmacyInfosRepository.delete(pharmacyInfos);
        }
    }
}
