package bzh.stack.apimovix.dto.importertoken;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Token d'importation pour l'API importer")
public class ImporterTokenDTO {

    @Schema(description = "Identifiant unique du token", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nom du token", example = "Token Pharmacie X")
    private String name;

    @Schema(description = "Token d'authentification (généré automatiquement)", example = "abc123xyz789...")
    private String token;

    @Schema(description = "Description du token", example = "Token pour l'importation des commandes de la pharmacie X")
    private String description;

    @Schema(description = "Indique si le token est actif", example = "true")
    private Boolean isActive;

    @Schema(description = "Indique si le token utilise le proxy beta", example = "false")
    private Boolean isBetaProxy;

    @Schema(description = "Code expéditeur associé", example = "EXP001")
    private String expCode;

    @Schema(description = "Date de création du token")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière utilisation du token")
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime lastUsedAt;
}
