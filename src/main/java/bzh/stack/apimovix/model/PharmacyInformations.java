package bzh.stack.apimovix.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "pharmacy_informations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cip", "id_account"})
})
public class PharmacyInformations {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "cip", nullable = false)
    private String cip;

    @ManyToOne
    @JoinColumn(name = "cip", insertable = false, updatable = false)
    @JsonIgnore
    private Pharmacy pharmacy;

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

    @Column(name = "informations", length = 4000)
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

    @Column(name = "commentaire", length = 4000)
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "id_zone")
    private Zone zone;

    @ManyToOne
    @JoinColumn(name = "id_account")
    private Account account;

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
