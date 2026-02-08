package bzh.stack.apimovix.scheduler;

import bzh.stack.apimovix.dto.tour.TourCreateDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.TourConfig;
import bzh.stack.apimovix.repository.TourConfigRepository;
import bzh.stack.apimovix.repository.tour.TourRepository;
import bzh.stack.apimovix.service.ProfileService;
import bzh.stack.apimovix.service.tour.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Scheduler pour la création automatique de tournées basées sur les TourConfig
 * S'exécute tous les jours à minuit pour créer les tournées du jour
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TourAutoCreationScheduler {

    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");

    private final TourConfigRepository tourConfigRepository;
    private final TourService tourService;
    private final TourRepository tourRepository;
    private final ProfileService profileService;

    /**
     * Tache executee au demarrage de l'application
     * Cree les tournees manquantes pour aujourd'hui dont l'heure est deja passee ou egale a l'heure courante
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createToursOnStartup() {
        log.info("=== Execution de la creation automatique des tournees au demarrage ===");
        LocalDate today = LocalDate.now(PARIS_ZONE);
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        LocalTime currentHour = LocalTime.now(PARIS_ZONE).withMinute(0).withSecond(0).withNano(0);
        int dayBit = 1 << (dayOfWeek.getValue() - 1);

        // Au demarrage, creer toutes les tournees dont l'heure est <= a l'heure courante
        List<TourConfig> activeConfigs = tourConfigRepository.findByActiveDay(dayBit);
        log.info("Nombre de configurations trouvees pour aujourd'hui: {}", activeConfigs.size());

        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;

        for (TourConfig config : activeConfigs) {
            // Ne creer que les tournees dont l'heure est deja passee ou egale
            LocalTime configHour = config.getTourHour() != null ? config.getTourHour() : LocalTime.of(8, 0);
            if (configHour.isAfter(currentHour)) {
                log.debug("Tournee ignoree (heure non atteinte: {}): {} (compte: {})",
                    configHour, config.getTourName(), config.getAccount().getSociete());
                continue;
            }
            try {
                if (createTourFromConfig(config, today)) {
                    successCount++;
                    log.info("Tournee creee avec succes pour config: {} (compte: {})",
                        config.getTourName(), config.getAccount().getSociete());
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Erreur lors de la creation de la tournee pour config: {} (compte: {})",
                    config.getTourName(), config.getAccount().getSociete(), e);
            }
        }

        log.info("=== Fin de la creation automatique des tournees au demarrage ===");
        log.info("Resultat: {} creations reussies, {} ignorees, {} erreurs",
            successCount, skippedCount, errorCount);
    }

    /**
     * Tache planifiee executee toutes les heures (a chaque heure pile)
     * Cree automatiquement les tournees dont l'heure de config correspond a l'heure courante
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Paris")
    @Transactional
    public void createDailyTours() {
        LocalTime currentHour = LocalTime.now(PARIS_ZONE).withMinute(0).withSecond(0).withNano(0);
        log.info("=== Demarrage de la creation automatique des tournees (heure: {}) ===", currentHour);
        LocalDate today = LocalDate.now(PARIS_ZONE);
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // Calculer le bit correspondant au jour (Lundi=bit 0, Dimanche=bit 6)
        int dayBit = 1 << (dayOfWeek.getValue() - 1);

        log.info("Jour: {} ({}), Bit: {}, Heure: {}", dayOfWeek, dayOfWeek.getValue(), dayBit, currentHour);

        // Recuperer les configurations actives pour ce jour et cette heure
        List<TourConfig> activeConfigs = tourConfigRepository.findByActiveDayAndHour(dayBit, currentHour);
        log.info("Nombre de configurations trouvees pour aujourd'hui a {}: {}", currentHour, activeConfigs.size());

        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;

        for (TourConfig config : activeConfigs) {
            try {
                if (createTourFromConfig(config, today)) {
                    successCount++;
                    log.info("Tournee creee avec succes pour config: {} (compte: {})",
                        config.getTourName(), config.getAccount().getSociete());
                } else {
                    skippedCount++;
                    log.info("Tournee deja existante, ignoree: {} (compte: {}, date: {})",
                        config.getTourName(), config.getAccount().getSociete(), today);
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Erreur lors de la creation de la tournee pour config: {} (compte: {})",
                    config.getTourName(), config.getAccount().getSociete(), e);
            }
        }

        log.info("=== Fin de la creation automatique des tournees ===");
        log.info("Resultat: {} creations reussies, {} ignorees (deja existantes), {} erreurs",
            successCount, skippedCount, errorCount);
    }

    /**
     * Crée une tournée à partir d'une configuration
     * @return true si la tournée a été créée, false si elle existait déjà
     */
    private boolean createTourFromConfig(TourConfig config, LocalDate date) {
        Account account = config.getAccount();

        // Vérifier si la tournée existe déjà pour ce compte, nom et date
        if (tourRepository.existsByAccountAndNameAndInitialDate(account, config.getTourName(), date)) {
            log.debug("Tournee deja existante - Nom: {}, Date: {}, Compte: {}",
                config.getTourName(), date, account.getSociete());
            return false;
        }

        // Récupérer le profil spécifique ou un profil par défaut
        Profil profil = config.getProfil();
        if (profil == null || !Boolean.TRUE.equals(profil.getIsActive())) {
            // Si le profil n'est pas défini ou inactif, chercher un profil par défaut
            Optional<Profil> optProfil = getDefaultProfil(account);
            if (optProfil.isEmpty()) {
                log.warn("Aucun profil trouve pour le compte: {}. Impossible de creer la tournee.",
                    account.getSociete());
                return false;
            }
            profil = optProfil.get();
            log.debug("Profil par defaut utilise: {} pour le compte: {}",
                profil.getIdentifiant(), account.getSociete());
        } else {
            log.debug("Profil de la config utilise: {} pour le compte: {}",
                profil.getIdentifiant(), account.getSociete());
        }

        // Créer le DTO pour la création de tournée
        TourCreateDTO tourCreateDTO = new TourCreateDTO();
        tourCreateDTO.setName(config.getTourName());
        tourCreateDTO.setColor(config.getTourColor());
        tourCreateDTO.setInitialDate(date);

        // Attribuer la zone si spécifiée dans la config
        if (config.getZone() != null) {
            tourCreateDTO.setZoneId(config.getZone().getId());
            log.debug("Zone attribuee: {} pour la tournee: {}",
                config.getZone().getName(), config.getTourName());
        }

        // Créer la tournée via le service avec assignation automatique du profil
        Tour createdTour = tourService.createTour(profil, tourCreateDTO, true);

        log.debug("Tournee creee - ID: {}, Nom: {}, Date: {}, Compte: {}, Profil: {}, Zone: {}",
            createdTour.getId(), createdTour.getName(), date, account.getSociete(),
            profil.getIdentifiant(), config.getZone() != null ? config.getZone().getName() : "aucune");

        return true;
    }

    /**
     * Récupère un profil par défaut pour un compte
     * Privilégie un profil admin si disponible, sinon prend le premier profil actif
     */
    private Optional<Profil> getDefaultProfil(Account account) {
        List<Profil> profils = profileService.findProfiles(account);

        // Essayer de trouver un profil admin actif
        Optional<Profil> adminProfil = profils.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAdmin()) && Boolean.TRUE.equals(p.getIsActive()))
                .findFirst();

        if (adminProfil.isPresent()) {
            log.debug("Profil admin trouve pour le compte: {}", account.getSociete());
            return adminProfil;
        }

        // Sinon, prendre le premier profil actif disponible
        Optional<Profil> activeProfil = profils.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .findFirst();

        if (activeProfil.isPresent()) {
            log.debug("Profil actif trouve pour le compte: {}", account.getSociete());
        }

        return activeProfil;
    }
}
