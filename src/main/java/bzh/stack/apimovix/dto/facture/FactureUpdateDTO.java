package bzh.stack.apimovix.dto.facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class FactureUpdateDTO {

    @JsonFormat(pattern = PATTERNS.DATE)
    private LocalDateTime dateFacture;

    private BigDecimal montantTTC;
    private Boolean isPaid;

    // PDF en base64 (optionnel, pour remplacer le PDF existant)
    private String pdfBase64;
}
