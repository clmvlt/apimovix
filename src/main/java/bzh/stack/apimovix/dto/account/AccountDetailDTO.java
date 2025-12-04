package bzh.stack.apimovix.dto.account;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class AccountDetailDTO {
    private UUID id;
    private String societe;
    private String address1;
    private String address2;
    private String postalCode;
    private String city;
    private String country;
    private Boolean isActive;
    
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime updatedAt;
    
    private Double latitude;
    private Double longitude;
    private Integer maxProfiles;
    private String anomaliesEmails;
    
    // Configuration SMTP (détails complets)
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpEnable;
    private Boolean smtpUseTls;
    private Boolean smtpUseSsl;
    private Boolean isScanCIP;
    private Boolean autoSendAnomalieEmails;
    private String logoUrl;

} 