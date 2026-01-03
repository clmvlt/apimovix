package bzh.stack.apimovix.dto.importertoken;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Données pour la mise à jour d'un token d'importation")
public class ImporterTokenUpdateDTO {

    @Schema(description = "Nouveau nom du token", example = "Token Pharmacie Y")
    private String name;

    @Schema(description = "Nouvelle description du token", example = "Token mis à jour pour la pharmacie Y")
    private String description;

    @Schema(description = "Activer ou désactiver le token", example = "true")
    private Boolean isActive;

    @Schema(description = "Indique si le token utilise le proxy beta", example = "false")
    private Boolean isBetaProxy;

    @Schema(description = "Code expéditeur associé", example = "EXP002")
    private String expCode;
}
