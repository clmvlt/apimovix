package bzh.stack.apimovix.dto.anomalie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating an anomaly comment")
public class AnomalieUpdateDTO {

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    @Schema(description = "Updated comment for the anomaly", example = "Updated comment regarding the delivery issue", required = true)
    private String comment;
}