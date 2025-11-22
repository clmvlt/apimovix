package bzh.stack.apimovix.dto.anomalie;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Paramètres de recherche pour les anomalies")
public class AnomalieSearchDTO {
    
    @Schema(description = "UUID du profil utilisateur pour filtrer", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "Date de début pour filtrer (format: yyyy-MM-dd)", example = "2025-01-01")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Le format de date doit être yyyy-MM-dd")
    private String dateDebut;
    
    @Schema(description = "Date de fin pour filtrer (format: yyyy-MM-dd)", example = "2025-12-31")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Le format de date doit être yyyy-MM-dd")
    private String dateFin;
    
    @Schema(description = "CIP de la pharmacie pour filtrer", example = "12345")
    private String cip;
    
    @Schema(description = "Code du type d'anomalie pour filtrer", example = "TYPE1")
    private String typeCode;

    @Schema(description = "Nombre maximum de résultats à retourner", example = "100")
    private Integer max;
} 