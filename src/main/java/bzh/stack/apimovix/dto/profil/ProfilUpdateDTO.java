package bzh.stack.apimovix.dto.profil;

import java.time.LocalDate;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ProfilUpdateDTO {
    private String identifiant;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Boolean isAdmin;
    private Boolean isWeb;
    private Boolean isMobile;
    
    @Email(message = GLOBAL.INVALID_FORMAT_EMAIL)
    private String email;

    private Boolean isStock;
    private Boolean isAvtrans;
    private Boolean isActive;

    @AssertTrue(message = GLOBAL.REQUIRED)
    private boolean isEmail() {
        return !Boolean.TRUE.equals(isWeb) || (email != null && !email.trim().isEmpty());
    }
}