package bzh.stack.apimovix.dto.tourconfig;

import bzh.stack.apimovix.dto.account.AccountDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO détaillé pour une configuration de tournée
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed tour configuration information")
public class TourConfigDetailDTO {

    @Schema(description = "Tour configuration unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Account information (company owning this tour configuration)")
    private AccountDTO account;

    @Schema(description = "Tour name", example = "Tournée Centre-Ville")
    private String tourName;

    @Schema(description = "Tour color in hexadecimal format", example = "#3498db")
    private String tourColor;

    @Schema(description = "Assigned zone for this tour", nullable = true)
    private ZoneDTO zone;

    @Schema(description = "Assigned profile for this tour", nullable = true)
    private ProfilDTO profil;

    @Schema(description = "Tour recurrence settings by day of week")
    private RecurrenceDTO recurrence;

    @Schema(description = "Hour at which the tour is automatically created (0-23)", example = "08:00")
    private LocalTime tourHour;

    @Schema(description = "Creation timestamp", example = "2024-12-04T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-12-04T15:45:00")
    private LocalDateTime updatedAt;
}
