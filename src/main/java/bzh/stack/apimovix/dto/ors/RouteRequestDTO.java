package bzh.stack.apimovix.dto.ors;

import java.util.List;

import bzh.stack.apimovix.dto.common.CoordsDTO;
import lombok.Data;

@Data
public class RouteRequestDTO {
    private List<CoordsDTO> coordinates;

    private Boolean returnCoords;

    public Boolean getReturnCoords() {
        return returnCoords != null ? returnCoords : false;
    }
}
