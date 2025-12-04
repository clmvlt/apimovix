package bzh.stack.apimovix.service;

import bzh.stack.apimovix.dto.tourconfig.TourConfigCreateDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigDetailDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigUpdateDTO;
import bzh.stack.apimovix.mapper.TourConfigMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.TourConfig;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.TourConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour gérer les configurations de tournées
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TourConfigService {

    private final TourConfigRepository tourConfigRepository;
    private final AccountRepository accountRepository;
    private final TourConfigMapper tourConfigMapper;

    /**
     * Crée une nouvelle configuration de tournée
     */
    public TourConfigDetailDTO createTourConfig(TourConfigCreateDTO createDTO) {
        // Vérifier que le compte existe
        Account account = accountRepository.findById(createDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé avec l'ID: " + createDTO.getAccountId()));

        // Vérifier qu'il n'existe pas déjà une config avec le même nom pour ce compte
        if (tourConfigRepository.existsByAccountIdAndTourName(createDTO.getAccountId(), createDTO.getTourName())) {
            throw new IllegalArgumentException("Une configuration de tournée avec ce nom existe déjà pour ce compte");
        }

        TourConfig tourConfig = tourConfigMapper.toEntity(createDTO);
        tourConfig.setAccount(account);

        TourConfig savedConfig = tourConfigRepository.save(tourConfig);
        return tourConfigMapper.toDetailDTO(savedConfig);
    }

    /**
     * Récupère une configuration de tournée par son ID
     */
    @Transactional(readOnly = true)
    public TourConfigDetailDTO getTourConfigById(UUID id) {
        TourConfig tourConfig = tourConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration de tournée non trouvée avec l'ID: " + id));
        return tourConfigMapper.toDetailDTO(tourConfig);
    }

    /**
     * Récupère toutes les configurations de tournée
     */
    @Transactional(readOnly = true)
    public List<TourConfigDetailDTO> getAllTourConfigs() {
        return tourConfigRepository.findAll().stream()
                .map(tourConfigMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les configurations de tournée pour un compte
     */
    @Transactional(readOnly = true)
    public List<TourConfigDetailDTO> getTourConfigsByAccount(UUID accountId) {
        return tourConfigRepository.findByAccountId(accountId).stream()
                .map(tourConfigMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les configurations actives pour un jour donné
     */
    @Transactional(readOnly = true)
    public List<TourConfigDetailDTO> getTourConfigsByDay(DayOfWeek dayOfWeek) {
        int dayBit = 1 << (dayOfWeek.getValue() - 1); // Lundi = 0b0000001, Mardi = 0b0000010, etc.
        return tourConfigRepository.findByActiveDay(dayBit).stream()
                .map(tourConfigMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les configurations par zone
     */
    @Transactional(readOnly = true)
    public List<TourConfigDetailDTO> getTourConfigsByZone(UUID zoneId) {
        return tourConfigRepository.findByZoneId(zoneId).stream()
                .map(tourConfigMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les configurations par profil
     */
    @Transactional(readOnly = true)
    public List<TourConfigDetailDTO> getTourConfigsByProfil(UUID profilId) {
        return tourConfigRepository.findByProfilId(profilId).stream()
                .map(tourConfigMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une configuration de tournée
     */
    public TourConfigDetailDTO updateTourConfig(UUID id, TourConfigUpdateDTO updateDTO) {
        TourConfig tourConfig = tourConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuration de tournée non trouvée avec l'ID: " + id));

        // Si on change le compte, vérifier qu'il existe
        if (updateDTO.getAccountId() != null && !updateDTO.getAccountId().equals(tourConfig.getAccount().getId())) {
            Account newAccount = accountRepository.findById(updateDTO.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé avec l'ID: " + updateDTO.getAccountId()));
            tourConfig.setAccount(newAccount);
        }

        // Vérifier le nom unique si modifié
        if (updateDTO.getTourName() != null && !updateDTO.getTourName().equals(tourConfig.getTourName())) {
            if (tourConfigRepository.existsByAccountIdAndTourName(tourConfig.getAccount().getId(), updateDTO.getTourName())) {
                throw new IllegalArgumentException("Une configuration de tournée avec ce nom existe déjà pour ce compte");
            }
        }

        tourConfigMapper.updateEntityFromDTO(updateDTO, tourConfig);
        TourConfig updatedConfig = tourConfigRepository.save(tourConfig);
        return tourConfigMapper.toDetailDTO(updatedConfig);
    }

    /**
     * Supprime une configuration de tournée
     */
    public void deleteTourConfig(UUID id) {
        if (!tourConfigRepository.existsById(id)) {
            throw new RuntimeException("Configuration de tournée non trouvée avec l'ID: " + id);
        }
        tourConfigRepository.deleteById(id);
    }
}
