package bzh.stack.apimovix.dto.tourconfig;

import java.util.UUID;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'une configuration de tournée
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tour configuration creation data")
public class TourConfigCreateDTO {

    @Schema(description = "Account ID (automatically set from authenticated user)", hidden = true)
    private UUID accountId;

    @NotBlank(message = "Le nom de la tournée est requis")
    @Size(max = 100, message = "Le nom de la tournée ne peut pas dépasser 100 caractères")
    @Schema(description = "Tour name", example = "Tournée Centre-Ville", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tourName;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être au format hexadécimal #RRGGBB")
    @Schema(description = "Tour color in hexadecimal format", example = "#3498db")
    private String tourColor;

    @Schema(description = "Zone assignment for this tour (optional)")
    private ZoneDTO zone;

    @Schema(description = "Assigned profile for this tour (optional)")
    private ProfilDTO profil;

    @NotNull(message = "La récurrence est requise")
    @Schema(description = "Tour recurrence by day of week", requiredMode = Schema.RequiredMode.REQUIRED)
    private RecurrenceDTO recurrence;
}
