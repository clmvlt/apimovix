package bzh.stack.apimovix.dto.account;

import bzh.stack.apimovix.util.PATTERNS;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Account/Company information")
public class AccountDTO {
    @Schema(description = "Account unique identifier", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID id;

    @Schema(description = "Company name", example = "Pharmacie Centrale")
    private String societe;

    @Schema(description = "Primary address line", example = "12 Rue de la République")
    private String address1;

    @Schema(description = "Secondary address line", example = "Bâtiment A")
    private String address2;

    @Schema(description = "Postal code", example = "75001")
    private String postalCode;

    @Schema(description = "City", example = "Paris")
    private String city;

    @Schema(description = "Country", example = "France")
    private String country;

    @Schema(description = "Account is active", example = "true")
    private Boolean isActive;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Latitude coordinate", example = "48.8566")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "2.3522")
    private Double longitude;

    @Schema(description = "Maximum number of profiles allowed", example = "10")
    private Integer maxProfiles;

    @Schema(description = "Comma-separated email addresses for anomaly notifications", example = "admin@pharmacie.fr,manager@pharmacie.fr")
    private String anomaliesEmails;

    @Schema(description = "Enable CIP scanning feature", example = "true")
    private Boolean isScanCIP;

    @Schema(description = "Automatically send anomaly emails", example = "true")
    private Boolean autoSendAnomalieEmails;

    @Schema(description = "Company logo URL", example = "/uploads/logos/logo.png")
    private String logoUrl;
}
