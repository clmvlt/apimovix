package bzh.stack.apimovix.dto.command;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CommandUpdateTarifDTO {
    
    private Double tarif;

    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @NotNull(message = GLOBAL.REQUIRED)
    @Valid
    private List<@Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID) String> commandIds = new ArrayList<>();
} 