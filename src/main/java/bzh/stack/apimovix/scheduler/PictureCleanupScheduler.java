package bzh.stack.apimovix.scheduler;

import bzh.stack.apimovix.service.picture.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PictureCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PictureCleanupScheduler.class);

    @Autowired
    private PictureService pictureService;

    /**
     * Exécute le nettoyage au démarrage de l'application
     */
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOnStartup() {
        logger.info("=== Execution du nettoyage des photos au demarrage ===");
        performDailyCleanup();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void performDailyCleanup() {
        logger.info("Starting scheduled picture cleanup...");
        try {
            int deletedCount = pictureService.deleteOldCommandPictures();
            logger.info("Scheduled picture cleanup completed. Deleted {} old pictures", deletedCount);
        } catch (Exception e) {
            logger.error("Error during scheduled picture cleanup", e);
        }
    }
}