package bzh.stack.apimovix.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import bzh.stack.apimovix.dto.logs.LogFileDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {

    private static final String LOGS_DIR = "logs/api";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<LogFileDTO> getAllLogFiles() throws IOException {
        List<LogFileDTO> logFiles = new ArrayList<>();
        Path logsPath = Paths.get(LOGS_DIR);

        if (!Files.exists(logsPath)) {
            return logFiles;
        }

        try (Stream<Path> paths = Files.walk(logsPath)) {
            logFiles = paths
                .filter(Files::isRegularFile)
                .map(this::convertToLogFileDTO)
                .collect(Collectors.toList());
        }

        return logFiles;
    }

    public String getLogFileContent(String relativePath) throws IOException {
        Path logPath = Paths.get(LOGS_DIR, relativePath);

        // Vérifier que le fichier est bien dans le répertoire logs (sécurité)
        if (!logPath.normalize().startsWith(Paths.get(LOGS_DIR).normalize())) {
            throw new SecurityException("Accès non autorisé au fichier");
        }

        if (!Files.exists(logPath)) {
            throw new IOException("Fichier non trouvé");
        }

        // Essayer de lire avec UTF-8, sinon avec ISO-8859-1 comme fallback
        try {
            return Files.readString(logPath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Tenter avec l'encodage par défaut du système
            byte[] bytes = Files.readAllBytes(logPath);
            return new String(bytes, Charset.defaultCharset());
        }
    }

    public void deleteLogFile(String relativePath) throws IOException {
        Path logPath = Paths.get(LOGS_DIR, relativePath);

        // Vérifier que le fichier est bien dans le répertoire logs (sécurité)
        if (!logPath.normalize().startsWith(Paths.get(LOGS_DIR).normalize())) {
            throw new SecurityException("Accès non autorisé au fichier");
        }

        if (!Files.exists(logPath)) {
            throw new IOException("Fichier non trouvé");
        }

        Files.delete(logPath);
    }

    public void deleteAllLogFiles() throws IOException {
        Path logsPath = Paths.get(LOGS_DIR);

        if (!Files.exists(logsPath)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(logsPath)) {
            paths
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Continue with other files
                    }
                });
        }
    }

    private LogFileDTO convertToLogFileDTO(Path path) {
        try {
            File file = path.toFile();
            long sizeBytes = Files.size(path);
            String sizeFormatted = formatFileSize(sizeBytes);

            LocalDateTime lastModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()),
                ZoneId.systemDefault()
            );

            // Chemin relatif depuis le dossier logs
            String relativePath = Paths.get(LOGS_DIR).relativize(path).toString();

            return new LogFileDTO(
                file.getName(),
                relativePath,
                sizeBytes,
                sizeFormatted,
                lastModified.format(DATE_FORMATTER)
            );
        } catch (IOException e) {
            return new LogFileDTO(path.getFileName().toString(), "", 0L, "0 B", "");
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
