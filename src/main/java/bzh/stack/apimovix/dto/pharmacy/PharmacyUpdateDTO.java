package bzh.stack.apimovix.dto.pharmacy;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PharmacyUpdateDTO {
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

    @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID)
    private UUID zoneId;
} 