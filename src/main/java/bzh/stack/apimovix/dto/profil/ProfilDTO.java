package bzh.stack.apimovix.dto.profil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Profile information")
public class ProfilDTO {
    @Schema(description = "Profile unique identifier", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID id;

    @Schema(description = "Profile username/login", example = "jdupont")
    private String identifiant;

    @Schema(description = "First name", example = "Jean")
    private String firstName;

    @Schema(description = "Last name", example = "Dupont")
    private String lastName;

    @Schema(description = "Birth date", example = "1990-05-15")
    private LocalDate birthday;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Administrator privileges", example = "false")
    private Boolean isAdmin;

    @Schema(description = "Web access enabled", example = "true")
    private Boolean isWeb;

    @Schema(description = "Mobile access enabled", example = "true")
    private Boolean isMobile;

    @Schema(description = "Email address", example = "jean.dupont@example.com")
    private String email;

    @Schema(description = "Stock management access", example = "false")
    private Boolean isStock;

    @Schema(description = "Transport management access", example = "true")
    private Boolean isAvtrans;

    @Schema(description = "Profile is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Profile picture URL", example = "/uploads/profiles/2024/12/04/profile.jpg")
    private String profilPicture;
}
