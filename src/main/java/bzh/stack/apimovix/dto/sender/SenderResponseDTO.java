package bzh.stack.apimovix.dto.sender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderResponseDTO {
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
    private String accountId;
    private String accountName;
}
