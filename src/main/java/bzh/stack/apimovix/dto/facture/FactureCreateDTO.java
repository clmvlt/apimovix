package bzh.stack.apimovix.dto.facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class FactureCreateDTO {

    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDateTime dateFacture;

    private BigDecimal montantTTC;
    private Boolean isPaid;
    private UUID accountId;

    // PDF en base64
    private String pdfBase64;
}
