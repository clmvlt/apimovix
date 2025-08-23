package bzh.stack.apimovix.dto.profil;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasswordChangeDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String currentPassword;
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String newPassword;
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    private String confirmPassword;
} 