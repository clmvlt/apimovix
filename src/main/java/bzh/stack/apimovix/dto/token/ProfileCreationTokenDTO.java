package bzh.stack.apimovix.dto.token;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.account.AccountDTO;
import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class ProfileCreationTokenDTO {

    private UUID id;
    private String token;
    private Boolean isUsed;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime usedAt;

    private AccountDTO account;
    private String notes;
}
