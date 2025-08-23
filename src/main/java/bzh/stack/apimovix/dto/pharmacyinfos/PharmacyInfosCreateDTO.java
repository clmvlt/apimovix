package bzh.stack.apimovix.dto.pharmacyinfos;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PharmacyInfosCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String cip;
    private String commentaire;
    private Boolean invalidGeocodage;


    private List<PictureDTO> pictures = new ArrayList<>();
}
