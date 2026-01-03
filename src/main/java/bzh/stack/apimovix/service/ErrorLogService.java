package bzh.stack.apimovix.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;

import bzh.stack.apimovix.dto.logs.LogContentDTO;
import bzh.stack.apimovix.dto.logs.LogFileDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private static final String ERROR_LOGS_DIR = "logs/errors";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log an exception with full request context
     */
    public void logException(HttpServletRequest request, Exception exception) {
        try {
            String authorization = request.getHeader("Authorization");
            String token = authorization != null ? authorization.replace("Bearer ", "") : "no-token";
            // Sanitize token to prevent directory traversal and filesystem issues
            token = token.replaceAll("[/\\\\:*?\"<>|]", "_");

            String logPath = String.format("%s/%s/%s",
                ERROR_LOGS_DIR,
                LocalDateTime.now().format(DATE_FORMATTER),
                token);

            File logFile = new File(logPath);
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created && !parentDir.exists()) {
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write("========================================\n");
                writer.write(String.format("=== ERROR at %s ===\n",
                    ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

                // Request information
                writer.write(String.format("IP: %s\n", getClientIpAddress(request)));
                writer.write(String.format("User-Agent: %s\n", request.getHeader("User-Agent")));
                writer.write(String.format("Method: %s\n", request.getMethod()));
                writer.write(String.format("URI: %s\n", request.getRequestURI()));

                if (request.getQueryString() != null) {
                    writer.write(String.format("Query String: %s\n", request.getQueryString()));
                }

                // Request body if available
                if (request instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
                    byte[] contentAsByteArray = requestWrapper.getContentAsByteArray();
                    if (contentAsByteArray.length > 0) {
                        String body = new String(contentAsByteArray, StandardCharsets.UTF_8);
                        writer.write(String.format("REQUEST BODY:\n%s\n", body));
                    }
                }

                // Exception information
                writer.write("\n=== EXCEPTION ===\n");
                writer.write(String.format("Type: %s\n", exception.getClass().getName()));
                writer.write(String.format("Message: %s\n", exception.getMessage()));

                // Full stack trace
                writer.write("Stack Trace:\n");
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                writer.write(sw.toString());

                writer.write("========================================\n\n");
            }
        } catch (Exception e) {
            // Silently ignore logging errors
        }
    }

    public List<LogFileDTO> getAllErrorLogFiles() throws IOException {
        List<LogFileDTO> logFiles = new ArrayList<>();
        Path logsPath = Paths.get(ERROR_LOGS_DIR);

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

    public LogContentDTO getErrorLogFileContent(String relativePath) throws IOException {
        Path logPath = Paths.get(ERROR_LOGS_DIR, relativePath);

        // Security check - prevent directory traversal
        if (!logPath.normalize().startsWith(Paths.get(ERROR_LOGS_DIR).normalize())) {
            throw new SecurityException("Acces non autorise au fichier");
        }

        if (!Files.exists(logPath)) {
            throw new IOException("Fichier non trouve");
        }

        String content;
        try {
            content = Files.readString(logPath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            byte[] bytes = Files.readAllBytes(logPath);
            content = new String(bytes, Charset.defaultCharset());
        }

        long sizeBytes = Files.size(logPath);

        return new LogContentDTO(content, relativePath, sizeBytes);
    }

    public void deleteErrorLogFile(String relativePath) throws IOException {
        Path logPath = Paths.get(ERROR_LOGS_DIR, relativePath);

        if (!logPath.normalize().startsWith(Paths.get(ERROR_LOGS_DIR).normalize())) {
            throw new SecurityException("Acces non autorise au fichier");
        }

        if (!Files.exists(logPath)) {
            throw new IOException("Fichier non trouve");
        }

        Files.delete(logPath);
    }

    public void deleteAllErrorLogFiles() throws IOException {
        Path logsPath = Paths.get(ERROR_LOGS_DIR);

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

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
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

            String relativePath = Paths.get(ERROR_LOGS_DIR).relativize(path).toString();

            return new LogFileDTO(
                file.getName(),
                relativePath,
                sizeBytes,
                sizeFormatted,
                lastModified.format(DISPLAY_DATE_FORMATTER)
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
