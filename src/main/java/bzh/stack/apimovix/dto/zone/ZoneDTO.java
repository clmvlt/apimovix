package bzh.stack.apimovix.dto.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Zone information")
public class ZoneDTO {
    @Schema(description = "Zone unique identifier", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID id;

    @Schema(description = "Zone name", example = "Centre-Ville")
    private String name;
}
