package bzh.stack.apimovix.dto.importer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Informations sur un colis à importer")
public class PackageImporterDTO {
    @Schema(description = "Identifiant unique du colis (code-barres)", example = "340002200000299670")
    private String id;

    @Schema(description = "Type de colis", example = "BAC")
    private String type;

    @Schema(description = "Désignation du contenu")
    private String designation;

    @Schema(description = "Quantité", example = "1")
    private Integer quantity;

    @Schema(description = "Poids en kg", example = "5.0")
    private Double weight;

    @Schema(description = "Volume en m³")
    private Double volume;

    @Schema(description = "Longueur en cm")
    private Double length;

    @Schema(description = "Largeur en cm")
    private Double width;

    @Schema(description = "Hauteur en cm")
    private Double height;

    @Schema(description = "Indique si le colis contient des produits frais", example = "true")
    private Boolean fresh;

    @Schema(description = "Numéro du colis", example = "4677")
    private String num;
}
