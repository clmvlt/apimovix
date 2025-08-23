package bzh.stack.apimovix.dto.tour;

import java.time.LocalDateTime;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TourStatusDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @Positive(message = GLOBAL.POSITIVE)
    private int id;
    private String name;
    private LocalDateTime createdAt;
    private ProfilDTO profil;
}
