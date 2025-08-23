package bzh.stack.apimovix.dto.command;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommandIdsDTO {
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @NotNull(message = GLOBAL.REQUIRED)
    private List<String> commandIds = new ArrayList<>();
}
