package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommandUpdateStatusDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @Positive(message = GLOBAL.POSITIVE)
    private int statusId;

    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @NotNull(message = GLOBAL.REQUIRED)
    @Valid
    private List<@Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID) String> commandIds = new ArrayList<>();

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    private Double latitude;
    private Double longitude;
}
