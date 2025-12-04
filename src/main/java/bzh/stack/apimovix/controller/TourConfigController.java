package bzh.stack.apimovix.controller;

import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.tourconfig.TourConfigCreateDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigDetailDTO;
import bzh.stack.apimovix.dto.tourconfig.TourConfigUpdateDTO;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.TourConfigService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour gérer les configurations de tournées
 * Filtre automatiquement les résultats par le compte de l'utilisateur connecté
 */
@RestController
@TokenRequired
@RequestMapping(value = "/tour-config", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tour Configuration", description = "API for tour configuration management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class TourConfigController {

    private final TourConfigService tourConfigService;

    @GetMapping
    @Operation(
            summary = "Get all tour configurations",
            description = "Retrieves all tour configurations for the connected user's account",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved tour configurations",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TourConfigDetailDTO.class)
                            )
                    )
            }
    )
    public ResponseEntity<?> getAllTourConfigs(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            List<TourConfigDetailDTO> tourConfigs = tourConfigService.getTourConfigsByAccount(profil.getAccount().getId());
            return MAPIR.ok(tourConfigs);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get tour configuration by ID",
            description = "Retrieves a specific tour configuration by its ID (must belong to connected user's account)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved tour configuration",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TourConfigDetailDTO.class)
                            )
                    )
            }
    )
    public ResponseEntity<?> getTourConfigById(
            @Parameter(description = "Tour configuration ID", required = true) @PathVariable UUID id,
            HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            TourConfigDetailDTO tourConfig = tourConfigService.getTourConfigById(id);

            // Vérifier que la config appartient bien au compte de l'utilisateur
            if (!tourConfig.getAccount().getId().equals(profil.getAccount().getId())) {
                return MAPIR.badRequest("Vous n'avez pas accès à cette configuration de tournée");
            }

            return MAPIR.ok(tourConfig);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a tour configuration",
            description = "Creates a new tour configuration for the connected user's account. The account ID is automatically set from the authenticated user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Tour configuration creation data",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TourConfigCreateDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created tour configuration",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TourConfigDetailDTO.class)
                            )
                    )
            }
    )
    public ResponseEntity<?> createTourConfig(
            @Valid @RequestBody TourConfigCreateDTO createDTO,
            HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            // Forcer l'accountId à celui de l'utilisateur connecté
            createDTO.setAccountId(profil.getAccount().getId());

            TourConfigDetailDTO created = tourConfigService.createTourConfig(createDTO);
            return MAPIR.ok(created);
        } catch (IllegalArgumentException e) {
            return MAPIR.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update tour configuration",
            description = "Updates an existing tour configuration (must belong to connected user's account). All fields are optional.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Tour configuration update data",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TourConfigUpdateDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated tour configuration",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TourConfigDetailDTO.class)
                            )
                    )
            }
    )
    public ResponseEntity<?> updateTourConfig(
            @Parameter(description = "Tour configuration ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody TourConfigUpdateDTO updateDTO,
            HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            // Vérifier que la config appartient bien au compte de l'utilisateur
            TourConfigDetailDTO existingConfig = tourConfigService.getTourConfigById(id);
            if (!existingConfig.getAccount().getId().equals(profil.getAccount().getId())) {
                return MAPIR.badRequest("Vous n'avez pas accès à cette configuration de tournée");
            }

            // S'assurer que l'accountId ne change pas
            updateDTO.setAccountId(null);

            TourConfigDetailDTO updated = tourConfigService.updateTourConfig(id, updateDTO);
            return MAPIR.ok(updated);
        } catch (IllegalArgumentException e) {
            return MAPIR.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tour configuration", description = "Deletes a tour configuration (must belong to connected user's account)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted tour configuration")
    })
    public ResponseEntity<?> deleteTourConfig(
            @Parameter(description = "Tour configuration ID", required = true) @PathVariable UUID id,
            HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            // Vérifier que la config appartient bien au compte de l'utilisateur
            TourConfigDetailDTO existingConfig = tourConfigService.getTourConfigById(id);
            if (!existingConfig.getAccount().getId().equals(profil.getAccount().getId())) {
                return MAPIR.badRequest("Vous n'avez pas accès à cette configuration de tournée");
            }

            tourConfigService.deleteTourConfig(id);
            return MAPIR.ok("Configuration de tournée supprimée avec succès");
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }
}
