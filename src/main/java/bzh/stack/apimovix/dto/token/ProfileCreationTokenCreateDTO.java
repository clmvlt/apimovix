package bzh.stack.apimovix.dto.token;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileCreationTokenCreateDTO {

    @NotNull(message = "L'ID du compte est requis")
    private UUID accountId;

    private String notes;
}
