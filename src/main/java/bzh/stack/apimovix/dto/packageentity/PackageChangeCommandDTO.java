package bzh.stack.apimovix.dto.packageentity;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PackageChangeCommandDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    private UUID newCommandId;
}
