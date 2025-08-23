package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandDTO {
    private UUID id;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime closeDate;
    private Integer tourOrder;
    private String tourColor;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime expDate;
    private String comment;
    private Boolean newPharmacy;
    private Double latitude;
    private Double longitude;
    private Double tarif;

    private List<PackageDTO> packages = new ArrayList<>();
    private PharmacyDTO pharmacy;
    private CommandStatusDTO status;
} 