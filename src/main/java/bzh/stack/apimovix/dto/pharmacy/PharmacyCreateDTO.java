package bzh.stack.apimovix.dto.pharmacy;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    private String informations;
    private String phone;
    private String fax;
    private String email;
    private Double latitude;
    private Double longitude;
    private String quality;
    private String first_name;
    private String last_name;
    private String commentaire;
    private UUID zoneId;
} 