package bzh.stack.apimovix.service.pharmacy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

    public PharmacyService(
            PharmacyRepository pharmacyRepository,
            PharmacyPictureRepository pharmacyPictureRepository,
            PharmacyInfosPictureRepository pharmacyInfosPictureRepository,
            PictureService pictureService,
            @Qualifier("pharmaciesCacheManager") CacheManager cacheManager, PharmacyMapper pharmacyMapper, ZoneRepository zoneRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacyPictureRepository = pharmacyPictureRepository;
        this.pharmacyInfosPictureRepository = pharmacyInfosPictureRepository;
        this.pictureService = pictureService;
        this.cacheManager = cacheManager;
        this.pharmacyMapper = pharmacyMapper;
        this.zoneRepository = zoneRepository;
    }

    private void clearTourCacheByDate(String cip) {
        Cache cache = cacheManager.getCache("pharmacies");
        if (cache != null) {
            cache.evict(cip);
        }
    }

    @Transactional(readOnly = true)
    public List<Pharmacy> findPharmacies() {
        return pharmacyRepository.findPharmacies();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pharmacies", key = "#cip", unless = "#result == null", cacheManager = "pharmaciesCacheManager")
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

        if (city != null) {
            for (Map.Entry<String, String> alias : GLOBAL.SEARCH_ALIASES.entrySet()) {
                if (city.toLowerCase().contains(alias.getKey())) {
                    city = city.toLowerCase().replace(alias.getKey(), alias.getValue());
                    break;
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

        return pharmacyRepository.searchPharmacies(
                name,
                city,
                pharmacySearchDTO.getPostalCode(),
                pharmacySearchDTO.getCip(),
                address);
    }

    @Transactional
    public PharmacyPicture createPharmacyPicture(Pharmacy pharmacy, String base64Image) {
        String fileName = pictureService.savePharmacyImage(pharmacy, base64Image);
        PharmacyPicture picture = null;
        clearTourCacheByDate(pharmacy.getCip());

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
        clearTourCacheByDate(pharmacy.getCip());

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
        clearTourCacheByDate(pharmacy.getCip());
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
        clearTourCacheByDate(cip);
        Pharmacy pharmacy = optPharmacy.get();
        pharmacyMapper.updateEntityFromDto(pharmacyUpdateDTO, pharmacy);
        if (pharmacyUpdateDTO.getZoneId() != null) {
            Optional<Zone> zoneOptional = zoneRepository.findZone(account, pharmacyUpdateDTO.getZoneId());
            if (zoneOptional.isPresent()) {
                pharmacy.setZone(zoneOptional.get());
            }
        }
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
            clearTourCacheByDate(cip);
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

        clearTourCacheByDate(pharmacy.getCip());

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