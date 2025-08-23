package bzh.stack.apimovix.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.dto.tarif.TarifCreateDTO;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.service.TarifService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import bzh.stack.apimovix.util.PATTERNS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/tarifs", produces = MediaType.APPLICATION_JSON_VALUE)
@AdminRequired
@Tag(name = "Tarifs", description = "API for managing delivery rates and pricing configurations")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
@RequiredArgsConstructor
public class TarifController {
    private final TarifService tarifService;

    @GetMapping
    @Operation(summary = "Get all rates", description = "Retrieves a list of all delivery rates configured for the authenticated user's account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of rates", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Tarif.class)))),
    })
    public ResponseEntity<?> getTarifs(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Tarif> tarifs = tarifService.findTarifsByAccount(profil.getAccount());
        return MAPIR.ok(tarifs);
    }

    @PostMapping
    @Operation(summary = "Create new rate", description = "Creates a new delivery rate configuration for the authenticated user's account", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created new rate", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tarif.class))),
    })
    public ResponseEntity<?> createTarif(
            HttpServletRequest request,
            @Parameter(description = "Rate configuration data", required = true, schema = @Schema(implementation = TarifCreateDTO.class)) @Valid @RequestBody TarifCreateDTO tarif) {
        Profil profil = (Profil) request.getAttribute("profil");
        tarif.setAccount(profil.getAccount());

        Tarif createdTarif = tarifService.createTarif(tarif);
        return MAPIR.created(createdTarif);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rate", description = "Deletes a specific delivery rate configuration", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted rate", content = @Content),
            @ApiResponse(responseCode = "404", description = "Rate not found", content = @Content),
    })
    public ResponseEntity<?> deleteTarif(
            HttpServletRequest request,
            @Parameter(description = "UUID of the rate to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Tarif> optTarif = tarifService.findTarif(uuid, profil.getAccount());

        if (optTarif.isEmpty()) {
            return MAPIR.notFound();
        }

        tarifService.deleteTarif(uuid);
        return MAPIR.deleted();
    }
}