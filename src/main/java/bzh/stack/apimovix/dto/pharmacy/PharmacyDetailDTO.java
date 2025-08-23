package bzh.stack.apimovix.dto.pharmacy;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PharmacyDetailDTO extends PharmacyDTO {
    private List<PharmacyPicture> pictures = new ArrayList<>();
} 