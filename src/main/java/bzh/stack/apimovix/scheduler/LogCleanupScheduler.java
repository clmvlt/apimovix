package bzh.stack.apimovix.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Component
@Slf4j
public class LogCleanupScheduler {

    private static final String API_LOGS_DIR = "logs/api";
    private static final String ERROR_LOGS_DIR = "logs/errors";
    private static final int RETENTION_DAYS = 14;

    /**
     * Tâche planifiée exécutée tous les jours à 2h du matin (en même temps que le nettoyage des photos)
     * Supprime les fichiers de logs de plus de 2 semaines
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldLogs() {
        log.info("=== Démarrage du nettoyage automatique des logs ===");

        int apiLogsDeleted = cleanupDirectory(API_LOGS_DIR);
        int errorLogsDeleted = cleanupDirectory(ERROR_LOGS_DIR);

        log.info("=== Fin du nettoyage des logs ===");
        log.info("Résultat: {} logs API supprimés, {} logs erreurs supprimés", apiLogsDeleted, errorLogsDeleted);
    }

    private int cleanupDirectory(String directory) {
        Path dirPath = Paths.get(directory);
        int deletedCount = 0;

        if (!Files.exists(dirPath)) {
            log.debug("Répertoire {} n'existe pas, rien à nettoyer", directory);
            return 0;
        }

        Instant cutoffDate = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);

        try (Stream<Path> paths = Files.walk(dirPath)) {
            var filesToDelete = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> isOlderThan(path, cutoffDate))
                    .toList();

            for (Path file : filesToDelete) {
                try {
                    Files.delete(file);
                    deletedCount++;
                    log.debug("Fichier supprimé: {}", file);
                } catch (IOException e) {
                    log.warn("Impossible de supprimer le fichier: {}", file, e);
                }
            }

            // Nettoyer les répertoires vides
            cleanupEmptyDirectories(dirPath);

        } catch (IOException e) {
            log.error("Erreur lors du nettoyage du répertoire: {}", directory, e);
        }

        return deletedCount;
    }

    private boolean isOlderThan(Path path, Instant cutoffDate) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toInstant().isBefore(cutoffDate);
        } catch (IOException e) {
            log.warn("Impossible de lire les attributs du fichier: {}", path);
            return false;
        }
    }

    private void cleanupEmptyDirectories(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(rootPath))
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount()) // Commencer par les plus profonds
                    .forEach(dir -> {
                        try {
                            if (isDirectoryEmpty(dir)) {
                                Files.delete(dir);
                                log.debug("Répertoire vide supprimé: {}", dir);
                            }
                        } catch (IOException e) {
                            // Ignorer les erreurs de suppression de répertoires
                        }
                    });
        } catch (IOException e) {
            log.warn("Erreur lors du nettoyage des répertoires vides", e);
        }
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.findFirst().isEmpty();
        }
    }
}
