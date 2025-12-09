package bzh.stack.apimovix.dto.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Réponse après envoi d'une commande")
public class SendCommandResponseDTO {

    @Schema(description = "Statut de la requête", example = "success")
    private String status;

    @Schema(description = "Message descriptif", example = "Commande créée avec succès")
    private String message;

    @Schema(description = "Identifiant unique de la commande créée")
    private UUID id_command;

    @Schema(description = "Liste des colis avec leurs codes-barres et URLs d'étiquettes")
    List<PackageDTO> packages = new ArrayList<>();
}
