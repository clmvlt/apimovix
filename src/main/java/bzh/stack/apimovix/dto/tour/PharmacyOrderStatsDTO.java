package bzh.stack.apimovix.dto.tour;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.service.ORSService;
import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PharmacyOrderStatsDTO {
    private PharmacyDTO pharmacy;
    private Long orderCount;
    private Long packageCount;
    private Double totalPrice;
    private Double averagePricePerOrder;
    private Double averageDistance;
    private List<CommandStatsDTO> commands;
    
    public PharmacyOrderStatsDTO(PharmacyDTO pharmacy, Long orderCount, Long packageCount, Double totalPrice, Double averageDistance, List<CommandStatsDTO> commands) {
        this.pharmacy = pharmacy;
        this.orderCount = orderCount;
        this.packageCount = packageCount;
        this.totalPrice = totalPrice;
        this.averagePricePerOrder = orderCount > 0 ? totalPrice / orderCount : 0.0;
        this.averageDistance = averageDistance;
        this.commands = commands;
    }
    
    @Data
    @NoArgsConstructor
    public static class CommandStatsDTO {
        private UUID id;

        @JsonFormat(pattern = PATTERNS.DATETIME)
        private LocalDateTime closeDate;
        private Integer tourOrder;

        @JsonFormat(pattern = PATTERNS.DATETIME)
        private LocalDateTime expDate;
        private String comment;
        private Boolean newPharmacy;
        private Double latitude;
        private Double longitude;
        private Long packageCount;
        private Double tarif;
        
        public CommandStatsDTO(Command command) {
            this.id = command.getId();
            this.closeDate = command.getCloseDate();
            this.tourOrder = command.getTourOrder();
            this.expDate = command.getExpDate();
            this.comment = command.getComment();
            this.newPharmacy = command.getNewPharmacy();
            this.latitude = command.getLatitude();
            this.longitude = command.getLongitude();
            this.packageCount = command.getPackages() != null ? (long) command.getPackages().size() : 0L;
            this.tarif = command.getTarif();
        }
        
        public CommandStatsDTO(Command command, List<bzh.stack.apimovix.model.Tarif> tarifs, ORSService orsService) {
            this.id = command.getId();
            this.closeDate = command.getCloseDate();
            this.tourOrder = command.getTourOrder();
            this.expDate = command.getExpDate();
            this.comment = command.getComment();
            this.newPharmacy = command.getNewPharmacy();
            this.latitude = command.getLatitude();
            this.longitude = command.getLongitude();
            this.packageCount = command.getPackages() != null ? (long) command.getPackages().size() : 0L;
            
            // Calculer le tarif : si null, utiliser le tarif estimé
            if (command.getTarif() != null) {
                this.tarif = command.getTarif();
            } else {
                // Calculer le tarif estimé basé sur la distance
                Double distance = orsService.calculateCommandDistance(command).orElse(0.0);
                Optional<bzh.stack.apimovix.model.Tarif> matchingTarif = tarifs.stream()
                        .filter(tarif -> distance <= tarif.getKmMax())
                        .min((t1, t2) -> Double.compare(t1.getKmMax(), t2.getKmMax()));
                
                this.tarif = matchingTarif.map(bzh.stack.apimovix.model.Tarif::getPrixEuro).orElse(0.0);
            }
        }
    }
} 