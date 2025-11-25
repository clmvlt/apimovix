package bzh.stack.apimovix.dto.ors;

import bzh.stack.apimovix.dto.common.CoordsDTO;
import lombok.Data;

/**
 * Représente un segment individuel d'un itinéraire avec la durée depuis le point précédent
 */
@Data
public class RouteSegmentDTO {
    private CoordsDTO coord;
    private Double distance; // Distance en km depuis le point précédent
    private Double duration; // Durée en minutes depuis le point précédent
    private Double cumulativeDistance; // Distance cumulée depuis le départ
    private Double cumulativeDuration; // Durée cumulée depuis le départ
}
