package bzh.stack.apimovix.dto.account;

import lombok.Data;

@Data
public class AccountUpdateDTO {
    private String societe;
    
    private String address1;
    
    private String address2;
    
    private Double latitude;
    
    private Double longitude;
    
    private String anomaliesEmails;
    
    // Configuration SMTP
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpEnable;
    private Boolean smtpUseTls;
    private Boolean smtpUseSsl;

} 