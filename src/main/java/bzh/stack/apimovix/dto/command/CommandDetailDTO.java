package bzh.stack.apimovix.dto.command;

import java.util.ArrayList;
import java.util.List;

import bzh.stack.apimovix.dto.importer.SenderDTO;
import bzh.stack.apimovix.model.Picture.CommandPicture;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CommandDetailDTO extends CommandDTO {
    private List<CommandPicture> pictures = new ArrayList<>();
    private SenderDTO sender;
}
