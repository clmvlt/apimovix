package bzh.stack.apimovix.controller;

import java.util.List;
import java.util.Optional;
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

import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.zone.ZoneAssignationDTO;
import bzh.stack.apimovix.dto.zone.ZoneDTO;
import bzh.stack.apimovix.dto.zone.ZoneDetailDTO;
import bzh.stack.apimovix.mapper.ZoneMapper;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Zone;
import bzh.stack.apimovix.service.ZoneService;
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
@RequestMapping(path = "/zones", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@Tag(name = "Zones", description = "API for managing delivery zones and pharmacy assignments")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class ZoneController {
    private final ZoneService zoneService;
    private final ZoneMapper zoneMapper;

    @GetMapping
    @Operation(summary = "Get all zones", description = "Retrieves a list of all delivery zones for the authenticated user's account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of zones", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ZoneDTO.class)))),
    })
    public ResponseEntity<?> getZones(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Zone> zones = zoneService.findZones(profil.getAccount());
        List<ZoneDTO> zoneDTOs = zones.stream()
                .map(zoneMapper::toDto)
                .collect(Collectors.toList());
        return MAPIR.ok(zoneDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID", description = "Retrieves detailed information about a specific delivery zone", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved zone details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ZoneDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Zone not found", content = @Content),
    })
    public ResponseEntity<?> getZone(
            HttpServletRequest request,
            @Parameter(description = "UUID of the zone to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Zone> optZone = zoneService.findZone(profil.getAccount(), uuid);
        if (optZone.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.ok(zoneMapper.toDetailDto(optZone.get()));
    }

    @PutMapping("/assign/{id}")
    @Operation(summary = "Assign pharmacies to zone", description = "Assigns one or more pharmacies to a specific delivery zone", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully assigned pharmacies to zone", content = @Content),
            @ApiResponse(responseCode = "404", description = "Zone or one or more pharmacies not found", content = @Content),
    })
    public ResponseEntity<?> assignPharmacyToZone(
            HttpServletRequest request,
            @Parameter(description = "UUID of the zone to assign pharmacies to", required = true, schema = @Schema(type = "string", format = "uuid")) @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") @PathVariable String id,
            @Parameter(description = "List of pharmacy CIP codes to assign", required = true, schema = @Schema(implementation = ZoneAssignationDTO.class)) @Valid @RequestBody ZoneAssignationDTO assignationDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        boolean success = zoneService.assignPharmacies(profil.getAccount(), assignationDTO.getCIPs(), uuid);
        if (!success) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PutMapping("/unassign")
    @Operation(summary = "Unassign pharmacies from zones", description = "Removes one or more pharmacies from their current delivery zones", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully unassigned pharmacies from zones", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more pharmacies not found", content = @Content),
    })
    public ResponseEntity<?> unassignPharmacyFromZone(
            HttpServletRequest request,
            @Parameter(description = "List of pharmacy CIP codes to unassign", required = true, schema = @Schema(implementation = ZoneAssignationDTO.class)) @Valid @RequestBody ZoneAssignationDTO assignationDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        boolean success = zoneService.unassignPharmacies(profil.getAccount(), assignationDTO.getCIPs());
        if (!success) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PostMapping()
    @Operation(summary = "Create zone", description = "Creates a new delivery zone with the specified name", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created zone", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ZoneDetailDTO.class))),
    })
    public ResponseEntity<?> createZone(
            HttpServletRequest request,
            @Parameter(description = "Zone creation data", required = true, schema = @Schema(implementation = ZoneDetailDTO.class)) @Valid @RequestBody ZoneDetailDTO createDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        Zone createdZone = zoneService.createZone(profil.getAccount(), createDTO.getName());
        return MAPIR.created(zoneMapper.toDetailDto(createdZone));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete zone", description = "Deletes a specific delivery zone and removes all pharmacy assignments", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted zone", content = @Content),
            @ApiResponse(responseCode = "404", description = "Zone not found", content = @Content),
    })
    public ResponseEntity<?> deleteZone(
            HttpServletRequest request,
            @Parameter(description = "UUID of the zone to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        boolean deleted = zoneService.deleteZone(profil.getAccount(), uuid);
        if (!deleted) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }
}