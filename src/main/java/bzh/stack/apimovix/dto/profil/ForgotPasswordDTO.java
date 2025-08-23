package bzh.stack.apimovix.dto.profil;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotBlank(message = GLOBAL.CANNOT_BE_EMPTY)
    @Email(message = "Format d'email invalide")
    private String email;
} 