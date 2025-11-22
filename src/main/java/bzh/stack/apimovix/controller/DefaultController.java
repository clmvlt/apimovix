package bzh.stack.apimovix.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.dto.common.ApiStatusDTO;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "API Status", description = "Endpoints to check API status and health")
public class DefaultController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/check")
    @Operation(summary = "Check API status", description = "Returns information about the API status, including version, database connection, environment and system metrics")
    @ApiResponse(responseCode = "200", description = "API status successfully retrieved", content = @Content(schema = @Schema(implementation = ApiStatusDTO.class)))
    public ResponseEntity<?> check() {
        String dbVersion = "Not Connected";
        boolean dbConnected = false;

        try {
            dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
            dbConnected = true;
        } catch (Exception e) {
        }

        String currentTime = LocalDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        double cpuUsage = osBean.getSystemLoadAverage();
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long freeMemory = totalMemory - usedMemory;

        ApiStatusDTO response = new ApiStatusDTO(
                appVersion,
                dbVersion,
                dbConnected,
                databaseUrl,
                "OK",
                currentTime,
                activeProfile,
                cpuUsage,
                totalMemory,
                usedMemory,
                freeMemory);

        return MAPIR.ok(response);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Page d'accueil", description = "Affiche la page d'accueil de l'API")
    public Resource home() {
        return new ClassPathResource("static/index.html");
    }
}
