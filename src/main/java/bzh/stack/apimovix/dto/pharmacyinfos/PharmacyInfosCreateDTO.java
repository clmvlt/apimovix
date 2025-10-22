package bzh.stack.apimovix.dto.pharmacyinfos;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PharmacyInfosCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    private String cip;
    
    @Size(max = 4000, message = "Le commentaire ne peut pas dépasser 4000 caractères")
    private String commentaire;
    
    private Boolean invalidGeocodage;


    private List<PictureDTO> pictures = new ArrayList<>();
}
