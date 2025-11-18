package bzh.stack.apimovix.dto.tour;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String name;

    private String color;

    @NotNull(message = GLOBAL.REQUIRED)
    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDate initialDate;

    private UUID zoneId;
}
