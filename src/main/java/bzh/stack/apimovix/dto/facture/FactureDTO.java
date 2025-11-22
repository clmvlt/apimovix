package bzh.stack.apimovix.dto.facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class FactureDTO {
    private UUID id;
    private String pdfUrl;

    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDateTime dateFacture;

    private BigDecimal montantTTC;
    private Boolean isPaid;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    private UUID accountId;
}
