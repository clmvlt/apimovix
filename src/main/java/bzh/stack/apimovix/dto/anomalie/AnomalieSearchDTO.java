package bzh.stack.apimovix.dto.anomalie;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Parametres de recherche pour les anomalies")
public class AnomalieSearchDTO {

    @Schema(description = "Recherche simple : cherche dans tous les champs (nom pharmacie, ville, cip, adresse)")
    private String query;

    // Champs detailles pour la recherche avancee
    @Schema(description = "UUID du profil utilisateur pour filtrer", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Date de debut pour filtrer (format: yyyy-MM-dd)", example = "2025-01-01")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Le format de date doit etre yyyy-MM-dd")
    private String dateDebut;

    @Schema(description = "Date de fin pour filtrer (format: yyyy-MM-dd)", example = "2025-12-31")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Le format de date doit etre yyyy-MM-dd")
    private String dateFin;

    @Schema(description = "CIP de la pharmacie pour filtrer", example = "12345")
    private String cip;

    @Schema(description = "Code du type d'anomalie pour filtrer", example = "TYPE1")
    private String typeCode;

    // Pagination
    @Schema(description = "Numero de page (commence a 0)", example = "0")
    private Integer page;

    @Schema(description = "Nombre d'elements par page", example = "20")
    private Integer size;
} 