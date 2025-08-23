package bzh.stack.apimovix.dto.ors;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.common.CoordsDTO;
import lombok.Data;

@Data
public class OptimizeRequestDTO {
    private List<CoordsDTO> coordinates = new ArrayList<>();
}
