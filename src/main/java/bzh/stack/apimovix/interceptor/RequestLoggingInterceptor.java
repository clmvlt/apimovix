package bzh.stack.apimovix.interceptor;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String LOG_DIRECTORY = "logs/api";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Store the start time for performance tracking
        request.setAttribute("startTime", System.currentTimeMillis());
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
        
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(String.format("========================================\n"));
            writer.write(String.format("=== REQUEST at %s ===\n", ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
            writer.write(String.format("Duration: %d ms\n", duration));
            writer.write(String.format("IP: %s\n", getClientIpAddress(request)));
            writer.write(String.format("User-Agent: %s\n", request.getHeader("User-Agent")));
            writer.write(String.format("Method: %s\n", request.getMethod()));
            writer.write(String.format("URI: %s\n", request.getRequestURI()));
            
            // Query parameters
            if (request.getQueryString() != null) {
                writer.write(String.format("Query String: %s\n", request.getQueryString()));
            }
            
            // Request headers
            writer.write("REQUEST HEADERS:\n");
            Map<String, String> headers = getRequestHeaders(request);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                // Mask sensitive headers
                String value = header.getKey().toLowerCase().contains("auth") ? "[MASKED]" : header.getValue();
                writer.write(String.format("  %s: %s\n", header.getKey(), value));
            }
            
            // Request body
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
                byte[] contentAsByteArray = requestWrapper.getContentAsByteArray();
                if (contentAsByteArray.length > 0) {
                    String body = new String(contentAsByteArray, StandardCharsets.UTF_8);
                    writer.write(String.format("REQUEST BODY: %s\n", body));
                }
            }
            
            writer.write(String.format("\n=== RESPONSE ===\n"));
            writer.write(String.format("Status: %d\n", response.getStatus()));
            writer.write(String.format("Content-Type: %s\n", response.getContentType()));
            
            // Response headers
            writer.write("RESPONSE HEADERS:\n");
            for (String headerName : response.getHeaderNames()) {
                writer.write(String.format("  %s: %s\n", headerName, response.getHeader(headerName)));
            }
            
            // Response body
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
                byte[] contentAsByteArray = responseWrapper.getContentAsByteArray();
                if (contentAsByteArray.length > 0) {
                    String body = new String(contentAsByteArray, StandardCharsets.UTF_8);
                    writer.write(String.format("RESPONSE BODY: %s\n", body));
                }
            }
            
            // Exception information
            if (ex != null) {
                writer.write(String.format("EXCEPTION: %s - %s\n", ex.getClass().getSimpleName(), ex.getMessage()));
            }
            
            writer.write("========================================\n\n");
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
    
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        if (headerNames != null) {
            for (String headerName : Collections.list(headerNames)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        
        return headers;
    }
} 