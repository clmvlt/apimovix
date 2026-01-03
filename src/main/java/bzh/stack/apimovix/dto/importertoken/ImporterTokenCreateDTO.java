package bzh.stack.apimovix.dto.importertoken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Données pour la création d'un token d'importation")
public class ImporterTokenCreateDTO {

    @NotBlank(message = "Le nom est requis")
    @Schema(description = "Nom du token", example = "Token Pharmacie X", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Description du token", example = "Token pour l'importation des commandes de la pharmacie X")
    private String description;

    @Schema(description = "Indique si le token utilise le proxy beta", example = "false", defaultValue = "false")
    private Boolean isBetaProxy;

    @Schema(description = "Code expéditeur associé", example = "EXP001")
    private String expCode;
}
