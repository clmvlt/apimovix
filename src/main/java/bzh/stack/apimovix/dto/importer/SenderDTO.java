package bzh.stack.apimovix.dto.importer;


import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SenderDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String code;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String postal_code;
    private String city;
    private String country;
    private String informations;
    private String quality;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private String fax;
    private Double latitude;
    private Double longitude;
} 