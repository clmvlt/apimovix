package bzh.stack.apimovix.dto.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String content;
    private boolean isHtml;
} 