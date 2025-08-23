package bzh.stack.apimovix.dto.ors;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class OptimizeResponseDTO {
    private List<String> optimizedOrder = new ArrayList<>();
    private String geometry;
}
