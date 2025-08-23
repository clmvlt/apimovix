package bzh.stack.apimovix.dto.tour;

import bzh.stack.apimovix.enums.ResponseStatusENUM;
import lombok.Data;

@Data
public class ValidateLoadingResponseDTO {
    private ResponseStatusENUM status;
    private String message;
    private Integer responseCode;
}
