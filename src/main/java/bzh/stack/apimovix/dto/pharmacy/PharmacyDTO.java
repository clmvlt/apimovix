package bzh.stack.apimovix.dto.pharmacy;

import bzh.stack.apimovix.dto.zone.ZoneDTO;
import bzh.stack.apimovix.model.Pharmacy;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PharmacyDTO {
    private String cip;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String postalCode;
    private String city;
    private String country;
    private String informations;
    private String phone;
    private String fax;
    private String email;
    private Double latitude;
    private Double longitude;
    private String quality;
    private String firstName;
    private String lastName;
    private Boolean neverOrdered;
    private String commentaire;
    private ZoneDTO zone;

    public PharmacyDTO(Pharmacy pharmacy) {
        this.cip = pharmacy.getCip();
        this.name = pharmacy.getName();
        this.address1 = pharmacy.getAddress1();
        this.address2 = pharmacy.getAddress2();
        this.address3 = pharmacy.getAddress3();
        this.postalCode = pharmacy.getPostalCode();
        this.city = pharmacy.getCity();
        this.country = pharmacy.getCountry();
        this.informations = pharmacy.getInformations();
        this.phone = pharmacy.getPhone();
        this.fax = pharmacy.getFax();
        this.email = pharmacy.getEmail();
        this.latitude = pharmacy.getLatitude();
        this.longitude = pharmacy.getLongitude();
        this.quality = pharmacy.getQuality();
        this.firstName = pharmacy.getFirstName();
        this.lastName = pharmacy.getLastName();
        this.neverOrdered = pharmacy.getNeverOrdered();
        this.commentaire = pharmacy.getCommentaire();
    }

    public PharmacyDTO(String cip, String name, String address, String city, String postalCode, Double latitude, Double longitude) {
        this.cip = cip;
        this.name = name;
        this.address1 = address;
        this.city = city;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
