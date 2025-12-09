package bzh.stack.apimovix.dto.pharmacy;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Informations sur le destinataire (pharmacie)")
public class PharmacyCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @Schema(description = "Code CIP de la pharmacie", example = "2111281", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cip;

    @Schema(description = "Nom de la pharmacie", example = "PHARMACIE DU LIERRE")
    private String name;

    @Schema(description = "Adresse ligne 1", example = "2 RUE DE PARIS")
    private String address1;

    @Schema(description = "Adresse ligne 2")
    private String address2;

    @Schema(description = "Adresse ligne 3")
    private String address3;

    @Schema(description = "Code postal", example = "35500")
    private String postal_code;

    @Schema(description = "Ville", example = "VITRE")
    private String city;

    @Schema(description = "Code pays (ISO 2 lettres)", example = "FR")
    private String country;

    @Size(max = 4000, message = "Les informations ne peuvent pas dépasser 4000 caractères")
    @Schema(description = "Informations complémentaires")
    private String informations;

    @Schema(description = "Numéro de téléphone")
    private String phone;

    @Schema(description = "Numéro de fax")
    private String fax;

    @Schema(description = "Adresse email")
    private String email;

    @Schema(description = "Latitude GPS")
    private Double latitude;

    @Schema(description = "Longitude GPS")
    private Double longitude;

    @Schema(description = "Qualité/titre")
    private String quality;

    @Schema(description = "Prénom du contact")
    private String first_name;

    @Schema(description = "Nom du contact")
    private String last_name;

    @Size(max = 4000, message = "Le commentaire ne peut pas dépasser 4000 caractères")
    @Schema(description = "Commentaire sur la pharmacie")
    private String commentaire;

    @Schema(description = "Identifiant de la zone de livraison")
    private UUID zoneId;

    @Schema(description = "Identifiant du compte associé")
    private UUID accountId;
} 