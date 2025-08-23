package bzh.stack.apimovix.interceptor;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String LOG_DIRECTORY = "logs/api";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Wrap the request to make it readable multiple times
        if (!(request instanceof ContentCachingRequestWrapper)) {
            return true;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, @Nullable Exception ex) throws Exception {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null ? authorization.replace("Bearer ", "") : "no-token";
        
        String logPath = String.format("%s/%s/%s", 
            LOG_DIRECTORY,
            LocalDateTime.now().format(DATE_FORMATTER),
            token);
            
        File logFile = new File(logPath);
        logFile.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(String.format("=== Request at %s ===\n", ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
            writer.write(String.format("IP: %s\n", request.getRemoteAddr()));
            writer.write(String.format("URL: %s %s\n", request.getMethod(), request.getRequestURI()));
            
            // Read request body
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
                byte[] contentAsByteArray = requestWrapper.getContentAsByteArray();
                if (contentAsByteArray.length > 0) {
                    String body = new String(contentAsByteArray, StandardCharsets.UTF_8);
                    writer.write(String.format("Body: %s\n", body));
                }
            }
            
            writer.write("\n");
        }
    }
} 