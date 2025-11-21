package bzh.stack.apimovix.dto.sender;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SenderCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String code;

    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String postalCode;
    private String city;
    private String country;
    private String informations;
    private String quality;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String fax;
    private Double latitude;
    private Double longitude;

    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String accountId;
}
