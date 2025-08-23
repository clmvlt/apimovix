package bzh.stack.apimovix.dto.account;

import lombok.Data;

@Data
public class SmtpConfigDTO {
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpEnable;
    private Boolean smtpUseTls;
    private Boolean smtpUseSsl;

} 