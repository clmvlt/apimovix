package bzh.stack.apimovix.dto.tourconfig;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la mise à jour d'une configuration de tournée
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tour configuration update data (all fields are optional)")
public class TourConfigUpdateDTO {

    @Schema(description = "Account ID (cannot be changed)", hidden = true)
    private UUID accountId;

    @Size(max = 100, message = "Le nom de la tournée ne peut pas dépasser 100 caractères")
    @Schema(description = "Tour name", example = "Tournée Centre-Ville Modifiée")
    private String tourName;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être au format hexadécimal #RRGGBB")
    @Schema(description = "Tour color in hexadecimal format", example = "#e74c3c")
    private String tourColor;

    @Schema(description = "Zone assignment for this tour (null to remove zone)")
    private ZoneDTO zone;

    @Schema(description = "Assigned profile for this tour (null to remove profile)")
    private ProfilDTO profil;

    @Schema(description = "Tour recurrence by day of week")
    private RecurrenceDTO recurrence;
}
