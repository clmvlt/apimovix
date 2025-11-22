package bzh.stack.apimovix.dto.profil;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProfilMobileUpdateDTO {
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String email;
    private String profilPicture;
}
