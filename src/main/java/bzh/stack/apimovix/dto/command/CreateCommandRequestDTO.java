package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.importer.CommandImporterDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommandRequestDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expedition_date;

    @NotNull(message = GLOBAL.REQUIRED)
    private String cip;
    
    @NotNull(message = GLOBAL.REQUIRED)
    private CommandImporterDTO command;
}