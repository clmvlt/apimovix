package bzh.stack.apimovix.dto.auth;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String identifiant;
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String password;
}
