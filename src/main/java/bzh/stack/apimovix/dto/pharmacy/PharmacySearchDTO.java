package bzh.stack.apimovix.dto.pharmacy;

import lombok.Data;

@Data
public class PharmacySearchDTO {
    private String postalCode;
    private String city;
    private String cip;
    private String address;
    private String name;
}
