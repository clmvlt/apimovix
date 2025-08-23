package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;

import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandStatusDTO {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private ProfilDTO profil;

    public CommandStatusDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public CommandStatusDTO(CommandStatus status) {
        this.id = status.getId();
        this.name = status.getName();
    }
}
