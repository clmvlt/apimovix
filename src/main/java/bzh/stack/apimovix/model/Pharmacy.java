package bzh.stack.apimovix.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "pharmacy")
public class Pharmacy {
    @Id
    @Column(name = "cip")
    private String cip;

    @Column(name = "name")
    private String name;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "address3")
    private String address3;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "informations")
    private String informations;

    @Column(name = "phone")
    private String phone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "email")
    private String email;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "quality")
    private String quality;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "never_ordered")
    private Boolean neverOrdered = true;

    @OneToMany(mappedBy = "pharmacy", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PharmacyPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "pharmacy")
    @JsonIgnore
    private List<PharmacyInfos> pharmacyInfos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_zone")
    private Zone zone;

    public void mapFromDTO(PharmacyCreateDTO recipientDTO) {
        this.cip = recipientDTO.getCip();
        this.name = recipientDTO.getName();
        this.address1 = recipientDTO.getAddress1();
        this.address2 = recipientDTO.getAddress2();
        this.address3 = recipientDTO.getAddress3();
        this.postalCode = recipientDTO.getPostal_code();
        this.city = recipientDTO.getCity();
        this.country = recipientDTO.getCountry();
        this.informations = recipientDTO.getInformations();
        this.phone = recipientDTO.getPhone();
        this.fax = recipientDTO.getFax();
        this.email = recipientDTO.getEmail();
        this.latitude = recipientDTO.getLatitude();
        this.longitude = recipientDTO.getLongitude();
        this.quality = recipientDTO.getQuality();
        this.firstName = recipientDTO.getFirst_name();
        this.lastName = recipientDTO.getLast_name();
    }

    @JsonIgnore
    public String getFullAdr() {
        StringBuilder fullAddress = new StringBuilder();
        if (address1 != null && !address1.trim().isEmpty()) {
            fullAddress.append(address1.trim());
        }
        if (address2 != null && !address2.trim().isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(address2.trim());
        }
        if (address3 != null && !address3.trim().isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(address3.trim());
        }
        return fullAddress.toString();
    }

    @JsonIgnore
    public String getFullCity() {
        StringBuilder fullAddress = new StringBuilder();
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            fullAddress.append(postalCode.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(city.trim());
        }
        return fullAddress.toString();
    }

    public Boolean getNeverOrdered() {
        return neverOrdered == null ? true : neverOrdered;
    }
}