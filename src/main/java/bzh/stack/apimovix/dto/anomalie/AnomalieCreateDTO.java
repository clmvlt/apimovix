package bzh.stack.apimovix.dto.anomalie;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnomalieCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String cip;

    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String code;
    
    private String other;
    private String actions;

    private List<String> barcodes = new ArrayList<>();
    private List<PictureDTO> pictures = new ArrayList<>();
}
