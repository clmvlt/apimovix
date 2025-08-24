package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandExpeditionDTO {
    private UUID id;
    private LocalDateTime closeDate;
    private Integer tourOrder;
    private LocalDateTime expDate;
    private String comment;
    private Boolean newPharmacy;
    private Double latitude;
    private Double longitude;

    private CommandTourDTO tour;
    private Integer packagesNumber;
    private Float totalWeight;
    private PharmacyDTO pharmacy;
    private String pharmacyCommentaire;
    private CommandStatusDTO status;

    public CommandExpeditionDTO(
        UUID id, 
        LocalDateTime closeDate, 
        Integer tourOrder, 
        LocalDateTime expDate, 
        String comment, 
        Boolean newPharmacy,
        Double latitude, 
        Double longitude, 
        Tour tour,
        Long packagesNumber, 
        Float totalWeight, 
        PharmacyDTO pharmacy, 
        CommandStatus status
    ) {
        this.id = id;
        this.closeDate = closeDate;
        this.tourOrder = tourOrder;
        this.expDate = expDate;
        this.comment = comment;
        this.newPharmacy = newPharmacy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tour = tour != null ? new CommandTourDTO(tour) : null;
        this.packagesNumber = packagesNumber != null ? packagesNumber.intValue() : 0;
        this.totalWeight = totalWeight != null ? totalWeight.floatValue() : 0;
        this.pharmacy = pharmacy;
        this.pharmacyCommentaire = pharmacy != null ? pharmacy.getCommentaire() : null;
        this.status = status != null ? new CommandStatusDTO(status) : null;
    }

    // Constructor for optimized query with pharmacy commentaire
    public CommandExpeditionDTO(
        UUID id,
        LocalDateTime closeDate,
        Integer tourOrder,
        LocalDateTime expDate,
        String comment,
        Boolean newPharmacy,
        Double latitude,
        Double longitude,
        CommandTourDTO tour,
        Long packagesNumber,
        Double totalWeight,
        PharmacyDTO pharmacy,
        String pharmacyCommentaire,
        CommandStatus status
    ) {
        this.id = id;
        this.closeDate = closeDate;
        this.tourOrder = tourOrder;
        this.expDate = expDate;
        this.comment = comment;
        this.newPharmacy = newPharmacy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tour = tour;
        this.packagesNumber = packagesNumber != null ? packagesNumber.intValue() : 0;
        this.totalWeight = totalWeight != null ? totalWeight.floatValue() : 0;
        this.pharmacy = pharmacy;
        this.pharmacyCommentaire = pharmacyCommentaire;
        this.status = status != null ? new CommandStatusDTO(status) : null;
    }
} 