package bzh.stack.apimovix.dto.pharmacy;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PharmacyCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String cip;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String postal_code;
    private String city;
    private String country;

    @Size(max = 4000, message = "Les informations ne peuvent pas dépasser 4000 caractères")
    private String informations;

    private String phone;
    private String fax;
    private String email;
    private Double latitude;
    private Double longitude;
    private String quality;
    private String first_name;
    private String last_name;

    @Size(max = 4000, message = "Le commentaire ne peut pas dépasser 4000 caractères")
    private String commentaire;

    private UUID zoneId;
    private UUID accountId;
} 