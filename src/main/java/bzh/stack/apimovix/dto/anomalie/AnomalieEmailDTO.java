package bzh.stack.apimovix.dto.anomalie;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for sending anomaly by email")
public class AnomalieEmailDTO {

    @Schema(description = "Optional list of email addresses to send to. If empty, will send to account emails", example = "[\"admin@example.com\", \"manager@example.com\"]")
    private List<@Email(message = "Invalid email format") String> emails;
}