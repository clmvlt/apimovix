package bzh.stack.apimovix.dto.importer;

import bzh.stack.apimovix.util.GLOBAL;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Informations sur l'expéditeur de la commande")
public class SenderDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @Schema(description = "Code unique de l'expéditeur", example = "MEZEGEL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Nom de l'expéditeur", example = "MEZEGEL")
    private String name;

    @Schema(description = "Adresse ligne 1", example = "45B rue Saint Thomas")
    private String address1;

    @Schema(description = "Adresse ligne 2", example = "null")
    private String address2;

    @Schema(description = "Adresse ligne 3", example = "null")
    private String address3;

    @Schema(description = "Code postal", example = "22120")
    private String postal_code;

    @Schema(description = "Ville", example = "HILLION")
    private String city;

    @Schema(description = "Code pays (ISO 2 lettres)", example = "FR")
    private String country;

    @Schema(description = "Informations complémentaires", example = "")
    private String informations;

    @Schema(description = "Qualité/titre")
    private String quality;

    @Schema(description = "Prénom du contact")
    private String first_name;

    @Schema(description = "Nom du contact")
    private String last_name;

    @Schema(description = "Adresse email")
    private String email;

    @Schema(description = "Numéro de téléphone")
    private String phone;

    @Schema(description = "Numéro de fax")
    private String fax;

    @Schema(description = "Latitude GPS")
    private Double latitude;

    @Schema(description = "Longitude GPS")
    private Double longitude;
} 