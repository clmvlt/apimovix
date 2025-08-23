package bzh.stack.apimovix.dto.zone;

import java.util.List;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ZoneDetailDTO extends ZoneDTO {
    private List<PharmacyDTO> pharmacies;
}
