package bzh.stack.apimovix.dto.tour;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.command.CommandDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TourDetailDTO extends TourDTO {
    private List<CommandDTO> commands = new ArrayList<>();
}
