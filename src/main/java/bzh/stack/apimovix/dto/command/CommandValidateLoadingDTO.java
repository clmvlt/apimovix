package bzh.stack.apimovix.dto.command;

import java.util.List;
import java.util.UUID;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CommandValidateLoadingDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID)
    private UUID commandId;

    @NotNull(message = GLOBAL.REQUIRED)
    private CommandStatusDTO status;

    private List<PackageDTO> packages;
    private String comment;
} 