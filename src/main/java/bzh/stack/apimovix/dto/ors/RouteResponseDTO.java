package bzh.stack.apimovix.dto.ors;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.common.CoordsDTO;
import lombok.Data;

@Data
public class RouteResponseDTO {
    private CoordsDTO coord;
    private Double distance;
    private Double duration;
    private String geometry;
    private List<List<Double>> coordinates = new ArrayList<>();
}
