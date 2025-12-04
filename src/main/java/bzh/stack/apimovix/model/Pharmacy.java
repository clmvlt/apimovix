package bzh.stack.apimovix.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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

    // Override getters pour utiliser pharmacyInformations si disponible
    public String getAddress1() {
        return (pharmacyInformations != null && pharmacyInformations.getAddress1() != null)
            ? pharmacyInformations.getAddress1() : this.address1;
    }

    public String getAddress2() {
        return (pharmacyInformations != null && pharmacyInformations.getAddress2() != null)
            ? pharmacyInformations.getAddress2() : this.address2;
    }

    public String getAddress3() {
        return (pharmacyInformations != null && pharmacyInformations.getAddress3() != null)
            ? pharmacyInformations.getAddress3() : this.address3;
    }

    public String getPostalCode() {
        return (pharmacyInformations != null && pharmacyInformations.getPostalCode() != null)
            ? pharmacyInformations.getPostalCode() : this.postalCode;
    }

    public String getCity() {
        return (pharmacyInformations != null && pharmacyInformations.getCity() != null)
            ? pharmacyInformations.getCity() : this.city;
    }

    public String getCountry() {
        return (pharmacyInformations != null && pharmacyInformations.getCountry() != null)
            ? pharmacyInformations.getCountry() : this.country;
    }

    public String getPhone() {
        return (pharmacyInformations != null && pharmacyInformations.getPhone() != null)
            ? pharmacyInformations.getPhone() : this.phone;
    }

    public String getFax() {
        return (pharmacyInformations != null && pharmacyInformations.getFax() != null)
            ? pharmacyInformations.getFax() : this.fax;
    }

    public String getEmail() {
        return (pharmacyInformations != null && pharmacyInformations.getEmail() != null)
            ? pharmacyInformations.getEmail() : this.email;
    }

    public Double getLatitude() {
        return (pharmacyInformations != null && pharmacyInformations.getLatitude() != null)
            ? pharmacyInformations.getLatitude() : this.latitude;
    }

    public Double getLongitude() {
        return (pharmacyInformations != null && pharmacyInformations.getLongitude() != null)
            ? pharmacyInformations.getLongitude() : this.longitude;
    }

    public String getQuality() {
        return (pharmacyInformations != null && pharmacyInformations.getQuality() != null)
            ? pharmacyInformations.getQuality() : this.quality;
    }

    public String getFirstName() {
        return (pharmacyInformations != null && pharmacyInformations.getFirstName() != null)
            ? pharmacyInformations.getFirstName() : this.firstName;
    }

    public String getLastName() {
        return (pharmacyInformations != null && pharmacyInformations.getLastName() != null)
            ? pharmacyInformations.getLastName() : this.lastName;
    }

    // Getters spécifiques pour informations, commentaire et neverOrdered (uniquement dans PharmacyInformations)
    public String getInformations() {
        return (pharmacyInformations != null) ? pharmacyInformations.getInformations() : null;
    }

    public String getCommentaire() {
        return (pharmacyInformations != null) ? pharmacyInformations.getCommentaire() : null;
    }

    public Boolean getNeverOrdered() {
        return (pharmacyInformations != null) ? pharmacyInformations.getNeverOrdered() : null;
    }

    @OneToMany(mappedBy = "pharmacy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PharmacyInformations> pharmacyInformationsList = new ArrayList<>();

    // Champ transient pour stocker le PharmacyInformations du compte actuel
    @jakarta.persistence.Transient
    private PharmacyInformations pharmacyInformations;

    @OneToMany(mappedBy = "pharmacy", fetch = FetchType.EAGER)
    @JsonManagedReference
    @jakarta.persistence.OrderBy("displayOrder ASC")
    private List<PharmacyPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "pharmacy")
    @JsonIgnore
    private List<PharmacyInfos> pharmacyInfos = new ArrayList<>();

    public void mapFromDTO(PharmacyCreateDTO recipientDTO) {
        this.cip = recipientDTO.getCip();
        this.name = recipientDTO.getName();
        this.address1 = recipientDTO.getAddress1();
        this.address2 = recipientDTO.getAddress2();
        this.address3 = recipientDTO.getAddress3();
        this.postalCode = recipientDTO.getPostal_code();
        this.city = recipientDTO.getCity();
        this.country = recipientDTO.getCountry();
        this.phone = recipientDTO.getPhone();
        this.fax = recipientDTO.getFax();
        this.email = recipientDTO.getEmail();
        this.latitude = recipientDTO.getLatitude();
        this.longitude = recipientDTO.getLongitude();
        this.quality = recipientDTO.getQuality();
        this.firstName = recipientDTO.getFirst_name();
        this.lastName = recipientDTO.getLast_name();
    }

    // Méthodes helper
    @JsonIgnore
    public String getFullAdr() {
        String addr1 = getAddress1();
        String addr2 = getAddress2();
        String addr3 = getAddress3();

        StringBuilder sb = new StringBuilder();
        if (addr1 != null && !addr1.isEmpty()) sb.append(addr1);
        if (addr2 != null && !addr2.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(addr2);
        }
        if (addr3 != null && !addr3.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(addr3);
        }
        return sb.toString();
    }

    @JsonIgnore
    public String getFullCity() {
        String pc = getPostalCode();
        String c = getCity();

        StringBuilder sb = new StringBuilder();
        if (pc != null && !pc.isEmpty()) sb.append(pc);
        if (c != null && !c.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(c);
        }
        return sb.toString();
    }

    @JsonIgnore
    public Zone getZone() {
        return pharmacyInformations != null ? pharmacyInformations.getZone() : null;
    }

    @JsonIgnore
    public Account getAccount() {
        return pharmacyInformations != null ? pharmacyInformations.getAccount() : null;
    }

    // Méthode pour charger le PharmacyInformations correspondant à un compte
    public void loadPharmacyInformationsForAccount(java.util.UUID accountId) {
        if (accountId != null && pharmacyInformationsList != null) {
            this.pharmacyInformations = pharmacyInformationsList.stream()
                .filter(pi -> pi.getAccount() != null && pi.getAccount().getId().equals(accountId))
                .findFirst()
                .orElse(null);
        }
    }

    // Méthode pour obtenir ou créer le PharmacyInformations pour un compte
    public PharmacyInformations getOrCreatePharmacyInformationsForAccount(Account account) {
        if (pharmacyInformationsList == null) {
            pharmacyInformationsList = new ArrayList<>();
        }

        PharmacyInformations pi = pharmacyInformationsList.stream()
            .filter(info -> info.getAccount() != null && info.getAccount().getId().equals(account.getId()))
            .findFirst()
            .orElse(null);

        if (pi == null) {
            pi = new PharmacyInformations();
            pi.setCip(this.cip);
            pi.setAccount(account);
            pharmacyInformationsList.add(pi);
        }

        return pi;
    }
}