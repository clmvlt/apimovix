package bzh.stack.apimovix.dto.account;

import lombok.Data;

@Data
public class AccountUpdateDTO {
    private String societe;
    
    private String address1;

    private String address2;

    private String postalCode;

    private String city;

    private String country;

    private Double latitude;
    
    private Double longitude;

    private Integer maxProfiles;

    private Boolean isActive;

    private String anomaliesEmails;
    
    // Configuration SMTP
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpEnable;
    private Boolean smtpUseTls;
    private Boolean smtpUseSsl;
    
    private Boolean isScanCIP;

    private Boolean autoSendAnomalieEmails;

    private Boolean autoCreateTour;

    // Logo en base64
    private String logo;

} 