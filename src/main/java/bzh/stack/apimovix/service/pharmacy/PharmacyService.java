package bzh.stack.apimovix.service.pharmacy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacySearchDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyUpdateDTO;
import bzh.stack.apimovix.mapper.PharmacyMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Zone;
import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import bzh.stack.apimovix.repository.ZoneRepository;
import bzh.stack.apimovix.repository.pharmacy.PharmacyInfosPictureRepository;
import bzh.stack.apimovix.repository.pharmacy.PharmacyPictureRepository;
import bzh.stack.apimovix.repository.pharmacy.PharmacyRepository;
import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.Valid;

@Service
public class PharmacyService {

    private final PharmacyMapper pharmacyMapper;

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyPictureRepository pharmacyPictureRepository;
    private final PharmacyInfosPictureRepository pharmacyInfosPictureRepository;
    private final PictureService pictureService;
    private final ZoneRepository zoneRepository;

    public PharmacyService(
            PharmacyRepository pharmacyRepository,
            PharmacyPictureRepository pharmacyPictureRepository,
            PharmacyInfosPictureRepository pharmacyInfosPictureRepository,
            PictureService pictureService,
            PharmacyMapper pharmacyMapper, ZoneRepository zoneRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacyPictureRepository = pharmacyPictureRepository;
        this.pharmacyInfosPictureRepository = pharmacyInfosPictureRepository;
        this.pictureService = pictureService;
        this.pharmacyMapper = pharmacyMapper;
        this.zoneRepository = zoneRepository;
    }


    @Transactional(readOnly = true)
    public List<Pharmacy> findPharmacies() {
        return pharmacyRepository.findPharmacies();
    }

    @Transactional(readOnly = true)
    public Optional<Pharmacy> findPharmacy(String cip) {
        return Optional.ofNullable(pharmacyRepository.findPharmacy(cip));
    }

    @Transactional(readOnly = true)
    public List<Pharmacy> searchPharmacies(PharmacySearchDTO pharmacySearchDTO) {
        String name = pharmacySearchDTO.getName();
        String city = pharmacySearchDTO.getCity();
        String address = pharmacySearchDTO.getAddress();

        if (name != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (name.toLowerCase().contains(alias.getKey())) {
                    name = name.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
                }
            }
        }

        String cityAlias = null;
        if (city != null) {
            String cityLower = city.toLowerCase().trim();

            if (cityLower.startsWith("st ") || cityLower.startsWith("st-")) {
                cityAlias = cityLower.replaceFirst("^st([\\s-])", "saint$1");
            } else if (cityLower.equals("st")) {
                cityAlias = "saint";
            } else if (cityLower.startsWith("saint ") || cityLower.startsWith("saint-")) {
                cityAlias = cityLower.replaceFirst("^saint([\\s-])", "st$1");
            } else if (cityLower.equals("saint")) {
                cityAlias = "st";
            } else if (cityLower.startsWith("ste ") || cityLower.startsWith("ste-")) {
                cityAlias = cityLower.replaceFirst("^ste([\\s-])", "sainte$1");
            } else if (cityLower.equals("ste")) {
                cityAlias = "sainte";
            } else if (cityLower.startsWith("sainte ") || cityLower.startsWith("sainte-")) {
                cityAlias = cityLower.replaceFirst("^sainte([\\s-])", "ste$1");
            } else if (cityLower.equals("sainte")) {
                cityAlias = "ste";
            }

            if (cityAlias == null) {
                String[] parts = cityLower.split("\\s+");
                if (parts.length > 1) {
                    boolean modified = false;
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i];
                        if (part.equals("st") || part.equals("st-")) {
                            parts[i] = part.replace("st", "saint");
                            modified = true;
                        } else if (part.equals("saint") || part.equals("saint-")) {
                            parts[i] = part.replace("saint", "st");
                            modified = true;
                        } else if (part.equals("ste") || part.equals("ste-")) {
                            parts[i] = part.replace("ste", "sainte");
                            modified = true;
                        } else if (part.equals("sainte") || part.equals("sainte-")) {
                            parts[i] = part.replace("sainte", "ste");
                            modified = true;
                        }
                    }
                    if (modified) {
                        cityAlias = String.join(" ", parts);
                    }
                }
            }
        }

        if (address != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (address.toLowerCase().contains(alias.getKey())) {
                    address = address.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
                }
            }
        }

        Integer maxResults = pharmacySearchDTO.getMax();
        if (maxResults == null || maxResults <= 0) {
            maxResults = 200; // Default value
        }

        return pharmacyRepository.searchPharmacies(
                name,
                city,
                cityAlias,
                pharmacySearchDTO.getPostalCode(),
                pharmacySearchDTO.getCip(),
                address,
                pharmacySearchDTO.getIsLocationValid(),
                maxResults,
                pharmacySearchDTO.getZoneId(),
                pharmacySearchDTO.getHasOrdered());
    }

    @Transactional
    public PharmacyPicture createPharmacyPicture(Pharmacy pharmacy, String base64Image) {
        String fileName = pictureService.savePharmacyImage(pharmacy, base64Image);
        PharmacyPicture picture = null;

        if (fileName != null) {
            try {
                picture = new PharmacyPicture();
                picture.setName(fileName);
                picture.setPharmacy(pharmacy);
                pharmacyPictureRepository.save(picture);

                pharmacy.getPictures().add(picture);
                pharmacyRepository.save(pharmacy);
            } catch (Exception e) {
                pictureService.deleteImage(fileName);
            }
        }

        return picture;
    }

    @Transactional
    public boolean deletePharmacyPhoto(Pharmacy pharmacy, UUID photoId) {
        PharmacyPicture pictureToDelete = pharmacy.getPictures().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElse(null);

        if (pictureToDelete == null) {
            return false;
        }

        boolean deleted = pictureService.deleteImage(pictureToDelete.getName());
        if (deleted) {
            pharmacy.getPictures().remove(pictureToDelete);
            pharmacyPictureRepository.delete(pictureToDelete);
            pharmacyRepository.save(pharmacy);
        }
        return deleted;
    }

    @Transactional
    public Pharmacy save(Pharmacy pharmacy) {
        return pharmacyRepository.save(pharmacy);
    }

    @Transactional
    public Pharmacy createPharmacy(@Valid PharmacyCreateDTO pharmacyDTO) {
        Optional<Pharmacy> optPharmacy = findPharmacy(pharmacyDTO.getCip());
        if (optPharmacy.isPresent()) {
            return optPharmacy.get();
        }
        Pharmacy pharmacy = new Pharmacy();
        Optional<Zone> zoneOptional = zoneRepository.findZone(pharmacyDTO.getZoneId());
        if (zoneOptional.isPresent()) {
            pharmacy.setZone(zoneOptional.get());
        }
        if (pharmacyDTO != null) {
            pharmacy.mapFromDTO(pharmacyDTO);
        }
        pharmacy.setNeverOrdered(true);
        return pharmacyRepository.save(pharmacy);
    }

    @Transactional
    public Optional<Pharmacy> updatePharmacy(Account account, @Valid PharmacyUpdateDTO pharmacyUpdateDTO, String cip) {
        Optional<Pharmacy> optPharmacy = findPharmacy(cip);
        if (optPharmacy.isEmpty()) {
            return Optional.empty();
        }
        Pharmacy pharmacy = optPharmacy.get();
        pharmacyMapper.updateEntityFromDto(pharmacyUpdateDTO, pharmacy);

        // Gérer la zone uniquement si le champ zoneId était présent dans la requête
        if (pharmacyUpdateDTO.isZoneIdWasSet()) {
            String zoneIdStr = pharmacyUpdateDTO.getZoneId();

            if (zoneIdStr == null || zoneIdStr.trim().isEmpty()) {
                // Si zoneId est null ou vide, supprimer la zone
                pharmacy.setZone(null);
            } else {
                // Sinon, assigner une nouvelle zone
                try {
                    UUID zoneId = UUID.fromString(zoneIdStr);
                    Optional<Zone> zoneOptional = zoneRepository.findZone(account, zoneId);
                    if (zoneOptional.isPresent()) {
                        pharmacy.setZone(zoneOptional.get());
                    }
                } catch (IllegalArgumentException e) {
                    // UUID invalide, on ignore
                }
            }
        }
        // Si zoneId n'était pas dans la requête, on ne touche pas à la zone

        return Optional.of(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public boolean deletePharmacy(String cip) {
        Optional<Pharmacy> optPharmacy = findPharmacy(cip);
        if (optPharmacy.isEmpty()) {
            return false;
        }

        Pharmacy pharmacy = optPharmacy.get();

        try {
            for (PharmacyPicture picture : pharmacy.getPictures()) {
                pictureService.deleteImage(picture.getName());
                pharmacyPictureRepository.delete(picture);
            }

            pharmacyRepository.delete(pharmacy);
                return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public PharmacyPicture copyPharmacyInfosPictureToPharmacy(Pharmacy pharmacy, UUID pharmacyInfosPictureId) {
        Optional<PharmacyInfosPicture> optPharmacyInfosPicture = pharmacyInfosPictureRepository.findById(pharmacyInfosPictureId);
        if (optPharmacyInfosPicture.isEmpty()) {
            return null;
        }

        PharmacyInfosPicture pharmacyInfosPicture = optPharmacyInfosPicture.get();

        // Vérifier que la photo appartient à une PharmacyInfos de la même pharmacie
        if (!pharmacyInfosPicture.getPharmacyInfos().getPharmacy().getCip().equals(pharmacy.getCip())) {
            return null;
        }

        // Copier le fichier image
        String newFileName = pictureService.copyPharmacyInfosImageToPharmacyImage(pharmacyInfosPicture, pharmacy);
        if (newFileName == null) {
            return null;
        }


        try {
            PharmacyPicture picture = new PharmacyPicture();
            picture.setName(newFileName);
            picture.setPharmacy(pharmacy);
            pharmacyPictureRepository.save(picture);

            pharmacy.getPictures().add(picture);
            pharmacyRepository.save(pharmacy);

            return picture;
        } catch (Exception e) {
            pictureService.deleteImage(newFileName);
            return null;
        }
    }
}
