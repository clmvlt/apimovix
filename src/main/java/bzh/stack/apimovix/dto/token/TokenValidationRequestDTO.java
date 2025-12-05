package bzh.stack.apimovix.dto.token;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenValidationRequestDTO {

    @NotBlank(message = "Le token est requis")
    private String token;
}
