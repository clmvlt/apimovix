package bzh.stack.apimovix.dto.common;

import lombok.Data;

@Data
public class PictureDTO {
    private String name;
    private String base64;
    private Integer displayOrder;
}
