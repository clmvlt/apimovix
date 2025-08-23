package bzh.stack.apimovix.dto.command;

import java.util.UUID;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommandUpdateOrderDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID)
    private UUID commandId;

    @NotNull(message = GLOBAL.REQUIRED)
    @Positive(message = GLOBAL.POSITIVE)
    private int tourOrder;
}
