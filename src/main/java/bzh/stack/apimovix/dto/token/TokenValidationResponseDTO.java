package bzh.stack.apimovix.dto.token;

import java.util.UUID;

import bzh.stack.apimovix.dto.account.AccountDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponseDTO {

    private boolean valid;
    private String message;
    private UUID accountId;
    private AccountDTO account;

    public static TokenValidationResponseDTO valid(UUID accountId, AccountDTO account) {
        return new TokenValidationResponseDTO(true, "Token valide", accountId, account);
    }

    public static TokenValidationResponseDTO invalid(String message) {
        return new TokenValidationResponseDTO(false, message, null, null);
    }
}
