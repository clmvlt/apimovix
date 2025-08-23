package bzh.stack.apimovix.dto.tour;

import java.util.List;

import bzh.stack.apimovix.dto.command.CommandValidateLoadingDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateLoadingRequestDTO {
    
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private List<CommandValidateLoadingDTO> commands;
}
