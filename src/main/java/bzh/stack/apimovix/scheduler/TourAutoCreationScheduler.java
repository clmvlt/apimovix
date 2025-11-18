package bzh.stack.apimovix.scheduler;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.service.tour.TourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TourAutoCreationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TourAutoCreationScheduler.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TourService tourService;

    /**
     * Exécuté au démarrage de l'application
     * Vérifie et crée les tours du jour si nécessaire
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        logger.info("Application startup - checking for tours to auto-create...");
        performAutoCreation();
    }

    /**
     * Exécuté tous les jours à 2h00 du matin
     * Duplique automatiquement les tours de la veille pour les comptes ayant autoCreateTour activé
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void autoCreateTours() {
        logger.info("Scheduled task - auto-creation of tours...");
        performAutoCreation();
    }

    /**
     * Logique commune d'auto-création des tours
     */
    private void performAutoCreation() {
        try {
            // Récupérer tous les comptes ayant l'auto-création de tours activée
            List<Account> accountsWithAutoCreate = accountRepository.findAllWithAutoCreateTourEnabled();

            if (accountsWithAutoCreate.isEmpty()) {
                logger.info("No accounts with auto-create tour enabled");
                return;
            }

            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();

            int totalToursCreated = 0;

            for (Account account : accountsWithAutoCreate) {
                try {
                    // Pour créer les tours, nous avons besoin d'un profil
                    // On utilise le premier profil disponible du compte, ou on log un warning
                    if (account.getProfils() == null || account.getProfils().isEmpty()) {
                        logger.warn("Account {} ({}) has autoCreateTour enabled but has no profiles. Skipping.",
                                  account.getId(), account.getSociete());
                        continue;
                    }

                    Profil profil = account.getProfils().get(0);

                    int toursCreated = tourService.duplicateToursFromDate(account, yesterday, today, profil);
                    totalToursCreated += toursCreated;

                    if (toursCreated > 0) {
                        logger.info("Created {} tours for account {} ({})",
                                  toursCreated, account.getId(), account.getSociete());
                    } else {
                        logger.debug("No tours created for account {} ({}) - either no tours found for yesterday or tours already exist for today",
                                   account.getId(), account.getSociete());
                    }

                } catch (Exception e) {
                    logger.error("Error creating tours for account {} ({})",
                               account.getId(), account.getSociete(), e);
                }
            }

            logger.info("Auto-creation of tours completed. Total tours created: {}", totalToursCreated);

        } catch (Exception e) {
            logger.error("Error during auto-creation of tours", e);
        }
    }
}
