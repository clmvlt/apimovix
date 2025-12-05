package bzh.stack.apimovix.dto.token;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilCreateWithTokenDTO {

    @NotBlank(message = "Le token est requis")
    private String token;

    @NotBlank(message = "L'email est requis")
    @Email(message = GLOBAL.INVALID_FORMAT_EMAIL)
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    private Boolean isAdmin = false;
}
