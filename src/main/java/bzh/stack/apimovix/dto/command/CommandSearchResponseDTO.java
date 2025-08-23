package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandSearchResponseDTO {
    private String id;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime closeDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime expDate;
    private String comment;
    private Boolean newPharmacy;
    private Double latitude;
    private Double longitude;

    private String pharmacyName;
    private String pharmacyCity;
    private String pharmacyCodePostal;
    private String pharmacyAddress1;
    private String pharmacyAddress2;
    private String pharmacyAddress3;

    public CommandSearchResponseDTO(
        UUID id,
        LocalDateTime closeDate,
        LocalDateTime expDate,
        String comment,
        Boolean newPharmacy,
        Double latitude,
        Double longitude,
        String pharmacyName,
        String pharmacyCity,
        String pharmacyCodePostal,
        String address1,
        String address2,
        String address3
    ) {
        this.id = id.toString();
        this.closeDate = closeDate;
        this.expDate = expDate;
        this.comment = comment;
        this.newPharmacy = newPharmacy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pharmacyName = pharmacyName;
        this.pharmacyCity = pharmacyCity;
        this.pharmacyCodePostal = pharmacyCodePostal;
        this.pharmacyAddress1 = address1;
        this.pharmacyAddress2 = address2;
        this.pharmacyAddress3 = address3;
    }
} 