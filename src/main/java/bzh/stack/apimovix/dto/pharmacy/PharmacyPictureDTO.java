package bzh.stack.apimovix.dto.pharmacy;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PharmacyPictureDTO {
    private UUID id;
    private String name;
    private Integer displayOrder;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    private String imagePath;
}
