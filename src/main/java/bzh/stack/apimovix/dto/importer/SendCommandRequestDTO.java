package bzh.stack.apimovix.dto.importer;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.GLOBAL;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Requête d'envoi de commande")
public class SendCommandRequestDTO {
    @NotNull(message = GLOBAL.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")
    @Schema(description = "Date d'expédition de la commande", example = "2025-12-05T19:34:26.5448103+01:00")
    private LocalDateTime expedition_date;

    @NotNull(message = GLOBAL.REQUIRED)
    @Schema(description = "Informations sur l'expéditeur")
    private SenderDTO sender;

    @NotNull(message = GLOBAL.REQUIRED)
    @Schema(description = "Informations sur le destinataire (pharmacie)")
    private RecipientImporterDTO recipient;

    @NotNull(message = GLOBAL.REQUIRED)
    @Schema(description = "Détails de la commande et des colis")
    private CommandImporterDTO command;
}