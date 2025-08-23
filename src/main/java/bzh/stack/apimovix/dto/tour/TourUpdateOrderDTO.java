package bzh.stack.apimovix.dto.tour;

import java.util.List;

import bzh.stack.apimovix.dto.command.CommandUpdateOrderDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourUpdateOrderDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private List<CommandUpdateOrderDTO> commands;
}
