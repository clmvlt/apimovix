package bzh.stack.apimovix.dto.tour;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourAssignDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    private UUID profilId;
}
