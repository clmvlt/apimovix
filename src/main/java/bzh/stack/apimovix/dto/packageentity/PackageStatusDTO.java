package bzh.stack.apimovix.dto.packageentity;

import java.time.LocalDateTime;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PackageStatusDTO {
    public PackageStatusDTO() {
    }

    public PackageStatusDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public PackageStatusDTO(PackageStatus status) {
        this.id = status.getId();
        this.name = status.getName();
    }

    @NotNull(message = GLOBAL.REQUIRED)
    @Positive(message = GLOBAL.POSITIVE)
    private int id;
    private String name;
    private LocalDateTime createdAt;
    private ProfilDTO profil;
}
