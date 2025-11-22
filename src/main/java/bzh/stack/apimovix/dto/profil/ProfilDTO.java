package bzh.stack.apimovix.dto.profil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ProfilDTO {
    private UUID id;
    private String identifiant;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isAdmin;
    private Boolean isWeb;
    private Boolean isMobile;
    private String email;
    private Boolean isStock;
    private Boolean isAvtrans;
    private Boolean isActive;
    private String profilPicture;
}
