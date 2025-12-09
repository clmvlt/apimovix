package bzh.stack.apimovix.dto.importer;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Détails de la commande à importer")
public class CommandImporterDTO {
    @Schema(description = "Numéro de transport/bon de livraison", example = "BLC_251224516-f")
    private String num_transport;

    @Schema(description = "Liste des colis de la commande")
    private List<PackageImporterDTO> packages = new ArrayList<>();

    @Schema(description = "Nombre total de colis", example = "1")
    private Integer number_of_packages;

    @Schema(description = "Volume total en m³")
    private Double volume;

    @Schema(description = "Poids total en kg")
    private Double weight;

    @Schema(description = "Date de clôture de la commande", example = "2025-12-05T19:34:26.2255816+01:00")
    private String close_date;
} 