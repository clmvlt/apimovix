package bzh.stack.apimovix.dto.importer;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendCommandRequestDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")
    private LocalDateTime expedition_date;

    @NotNull(message = GLOBAL.REQUIRED)
    private SenderDTO sender;
    
    @NotNull(message = GLOBAL.REQUIRED)
    private PharmacyCreateDTO recipient;
    
    @NotNull(message = GLOBAL.REQUIRED)
    private CommandImporterDTO command;
}