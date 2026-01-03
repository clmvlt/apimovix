package bzh.stack.apimovix.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.dto.importertoken.ImporterTokenCreateDTO;
import bzh.stack.apimovix.dto.importertoken.ImporterTokenDTO;
import bzh.stack.apimovix.dto.importertoken.ImporterTokenUpdateDTO;
import bzh.stack.apimovix.mapper.ImporterTokenMapper;
import bzh.stack.apimovix.model.ImporterToken;
import bzh.stack.apimovix.service.ImporterTokenService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/importer-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@HyperAdminRequired
@Tag(name = "Importer Tokens", description = "Gestion des tokens d'importation pour l'API importer (HyperAdmin)")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class ImporterTokenController {

    private final ImporterTokenService importerTokenService;
    private final ImporterTokenMapper importerTokenMapper;

    @GetMapping
    @Operation(
        summary = "Liste tous les tokens d'importation",
        description = "Récupère la liste complète de tous les tokens d'importation enregistrés",
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des tokens récupérée avec succès",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = ImporterTokenDTO.class))))
        }
    )
    public ResponseEntity<?> findAll() {
        List<ImporterToken> tokens = importerTokenService.findAll();
        List<ImporterTokenDTO> tokenDTOs = tokens.stream()
            .map(importerTokenMapper::toDto)
            .collect(Collectors.toList());
        return MAPIR.ok(tokenDTOs);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupère un token par son ID",
        description = "Récupère les détails d'un token d'importation spécifique à partir de son identifiant UUID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token trouvé",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ImporterTokenDTO.class))),
            @ApiResponse(responseCode = "404", description = GLOBAL.ERROR_404, content = @Content)
        }
    )
    public ResponseEntity<?> findById(
            @Parameter(description = "UUID du token à récupérer", required = true,
                schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID id) {
        return importerTokenService.findById(id)
            .<ResponseEntity<?>>map(token -> MAPIR.ok(importerTokenMapper.toDto(token)))
            .orElseGet(MAPIR::notFound);
    }

    @PostMapping
    @Operation(
        summary = "Crée un nouveau token d'importation",
        description = "Crée un nouveau token d'importation avec un token unique généré automatiquement",
        responses = {
            @ApiResponse(responseCode = "201", description = "Token créé avec succès",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ImporterTokenDTO.class)))
        }
    )
    public ResponseEntity<?> create(
            @Parameter(description = "Données de création du token", required = true)
            @Valid @RequestBody ImporterTokenCreateDTO request) {
        ImporterToken token = importerTokenService.create(
            request.getName(),
            request.getDescription(),
            request.getIsBetaProxy(),
            request.getExpCode()
        );
        return MAPIR.created(importerTokenMapper.toDto(token));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Met à jour un token d'importation",
        description = "Met à jour les informations d'un token d'importation existant. Seuls les champs fournis seront mis à jour.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token mis à jour avec succès",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ImporterTokenDTO.class))),
            @ApiResponse(responseCode = "404", description = GLOBAL.ERROR_404, content = @Content)
        }
    )
    public ResponseEntity<?> update(
            @Parameter(description = "UUID du token à mettre à jour", required = true,
                schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID id,
            @Parameter(description = "Données de mise à jour du token", required = true)
            @Valid @RequestBody ImporterTokenUpdateDTO request) {
        try {
            ImporterToken token = importerTokenService.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getIsActive(),
                request.getIsBetaProxy(),
                request.getExpCode()
            );
            return MAPIR.ok(importerTokenMapper.toDto(token));
        } catch (RuntimeException e) {
            return MAPIR.notFound();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprime un token d'importation",
        description = "Supprime définitivement un token d'importation. Cette action est irréversible.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token supprimé avec succès",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(type = "string", example = "Token supprimé"))),
            @ApiResponse(responseCode = "404", description = GLOBAL.ERROR_404, content = @Content)
        }
    )
    public ResponseEntity<?> delete(
            @Parameter(description = "UUID du token à supprimer", required = true,
                schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID id) {
        if (importerTokenService.findById(id).isEmpty()) {
            return MAPIR.notFound();
        }
        importerTokenService.delete(id);
        return MAPIR.ok("Token supprimé");
    }
}
