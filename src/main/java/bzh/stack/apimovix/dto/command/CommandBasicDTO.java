package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandBasicDTO {
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

    private PharmacyDTO pharmacy;
} 