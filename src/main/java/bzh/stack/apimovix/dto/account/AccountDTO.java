package bzh.stack.apimovix.dto.account;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class AccountDTO {
    private UUID id;
    private String societe;
    private String address1;
    private String address2;
    private Boolean isActive;
    
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime updatedAt;
    
    private Double latitude;
    private Double longitude;
    private Integer maxProfiles;
    private String anomaliesEmails;
    private Boolean isScanCIP;
    private Boolean autoSendAnomalieEmails;
    private Boolean autoCreateTour;
}
