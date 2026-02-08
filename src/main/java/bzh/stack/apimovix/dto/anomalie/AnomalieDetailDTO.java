package bzh.stack.apimovix.dto.anomalie;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.command.CommandSimpleDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.model.Picture.AnomaliePicture;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AnomalieDetailDTO extends AnomalieDTO {
    private List<AnomaliePicture> pictures = new ArrayList<>();
    private List<PackageDTO> packages = new ArrayList<>();
    private CommandSimpleDTO command;
} 