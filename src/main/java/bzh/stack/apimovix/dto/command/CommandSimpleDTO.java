package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandSimpleDTO {
    private UUID id;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime closeDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime expDate;

    private CommandStatusDTO status;
}
