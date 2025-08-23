package bzh.stack.apimovix.dto.tarif;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.util.GLOBAL;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TarifCreateDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @DecimalMin(value = "0.0", message = GLOBAL.FLOAT)
    private Double kmMax;

    @NotNull(message = GLOBAL.REQUIRED)
    @DecimalMin(value = "0.0", message = GLOBAL.FLOAT)
    private Double prixEuro;
    
    @JsonIgnore
    private Account account;
}
