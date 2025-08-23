package bzh.stack.apimovix.dto.profil;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String token;
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String newPassword;
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String confirmPassword;
} 