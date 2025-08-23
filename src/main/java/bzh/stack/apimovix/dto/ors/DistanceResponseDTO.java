package bzh.stack.apimovix.dto.ors;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DistanceResponseDTO {
    private List<RouteResponseDTO> distances = new ArrayList<>();
}
