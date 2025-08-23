package bzh.stack.apimovix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bzh.stack.apimovix.dto.importer.SenderDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sender")
public class Sender {
    
    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "address1", length = 50)
    private String address1;

    @Column(name = "address2", length = 50)
    private String address2;

    @Column(name = "address3", length = 50)
    private String address3;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "informations", columnDefinition = "TEXT")
    private String informations;

    @Column(name = "quality", length = 4)
    private String quality;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "fax", length = 50)
    private String fax;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "id_account", nullable = false)
    private Account account;

    public void mapFromDTO(SenderDTO senderDTO) {
        this.code = senderDTO.getCode();
        this.name = senderDTO.getName();
        this.address1 = senderDTO.getAddress1();
        this.address2 = senderDTO.getAddress2();
        this.address3 = senderDTO.getAddress3();
        this.postalCode = senderDTO.getPostal_code();
        this.city = senderDTO.getCity();
        this.country = senderDTO.getCountry();
        this.informations = senderDTO.getInformations();
        this.quality = senderDTO.getQuality();
        this.firstName = senderDTO.getFirst_name();
        this.lastName = senderDTO.getLast_name();
        this.email = senderDTO.getEmail();
        this.phone = senderDTO.getPhone();
        this.fax = senderDTO.getFax();
        this.latitude = senderDTO.getLatitude();
        this.longitude = senderDTO.getLongitude();
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
} 