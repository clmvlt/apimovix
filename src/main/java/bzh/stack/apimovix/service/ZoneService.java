package bzh.stack.apimovix.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Zone;
import bzh.stack.apimovix.repository.ZoneRepository;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final PharmacyService pharmacyService;

    @Transactional
    public Zone createZone(Account account, String name) {
        Zone zone = new Zone();
        zone.setId(UUID.randomUUID());
        zone.setName(name);
        zone.setAccount(account);
        return zoneRepository.save(zone);
    }

    @Transactional
    public boolean deleteZone(Account account, UUID zoneId) {
        return findZone(account, zoneId)
            .map(zone -> {
                zoneRepository.delete(zone);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean unassignPharmacy(Account account, String cip) {
        return pharmacyService.findPharmacy(cip)
            .map(pharmacy -> {
                pharmacy.setZone(null);
                pharmacyService.save(pharmacy);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean assignPharmacy(Account account, String cip, UUID zoneId) {
        Optional<Zone> optZone = findZone(account, zoneId);
        if (optZone.isEmpty()) {
            return false;
        }

        return pharmacyService.findPharmacy(cip)
            .map(pharmacy -> {
                pharmacy.setZone(optZone.get());
                pharmacyService.save(pharmacy);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean unassignPharmacies(Account account, List<String> cips) {
        List<Pharmacy> pharmacies = cips.stream()
            .map(pharmacyService::findPharmacy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (pharmacies.isEmpty()) {
            return false;
        }

        pharmacies.forEach(pharmacy -> {
            pharmacy.setZone(null);
            pharmacyService.save(pharmacy);
        });

        return true;
    }

    @Transactional
    public boolean assignPharmacies(Account account, List<String> cips, UUID zoneId) {
        Optional<Zone> optZone = findZone(account, zoneId);
        if (optZone.isEmpty()) {
            return false;
        }

        Zone zone = optZone.get();
        List<Pharmacy> pharmacies = cips.stream()
            .map(pharmacyService::findPharmacy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (pharmacies.isEmpty()) {
            return false;
        }

        pharmacies.forEach(pharmacy -> {
            pharmacy.setZone(zone);
            pharmacyService.save(pharmacy);
        });

        return true;
    }

    @Transactional(readOnly = true)
    public Optional<Zone> findZone(Account account, UUID zoneId) {
        return zoneRepository.findZone(account, zoneId);
    }

    @Transactional(readOnly = true)
    public List<Zone> findZones(Account account) {
        return zoneRepository.findZones(account);
    }
} 