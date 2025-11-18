package bzh.stack.apimovix.dto.pharmacy;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
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

    @Size(max = 4000, message = "Les informations ne peuvent pas dépasser 4000 caractères")
    private String informations;

    private String phone;
    private String fax;
    private String email;
    private Double latitude;
    private Double longitude;
    private String quality;
    private String first_name;
    private String last_name;

    @Size(max = 4000, message = "Le commentaire ne peut pas dépasser 4000 caractères")
    private String commentaire;

    // Permet d'accepter null explicitement pour supprimer la zone
    @JsonProperty("zoneId")
    private String zoneId;

    // Flag pour savoir si zoneId était présent dans la requête JSON
    private transient boolean zoneIdWasSet = false;

    @JsonProperty("zoneId")
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
        this.zoneIdWasSet = true;
    }
} 