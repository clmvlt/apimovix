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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.pharmacyinfos.PharmacyInfosCreateDTO;
import bzh.stack.apimovix.dto.pharmacyinfos.PharmacyInfosDTO;
import bzh.stack.apimovix.mapper.PharmacyInfosMapper;
import bzh.stack.apimovix.model.PharmacyInfos;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.pharmacy.PharmacyInfosService;
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
@RequestMapping(value = "/pharmacy-infos", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@Tag(name = "Pharmacy Information", description = "API for managing additional pharmacy information")
public class PharmacyInfosController {

    private final PharmacyInfosMapper pharmacyInfosMapper;
    private final PharmacyInfosService pharmacyInfosService;

    @GetMapping
    @Operation(summary = "Get all pharmacy information", description = "Retrieves a list of all pharmacy information entries for the authenticated user's account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of pharmacy information", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PharmacyInfosDTO.class)))),
    })
    public ResponseEntity<?> getAllPharmacyInfos(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<PharmacyInfos> pharmaciesInfos = pharmacyInfosService.findPharmaciesInfos(profil.getAccount());
        List<PharmacyInfosDTO> pharmlPharmacyInfosDTOs = pharmaciesInfos.stream()
                .map(pharmacyInfosMapper::toDto)
                .collect(Collectors.toList());
        return MAPIR.ok(pharmlPharmacyInfosDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pharmacy information by ID", description = "Retrieves detailed information about a specific pharmacy information entry using its UUID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy information", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyInfos.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy information not found", content = @Content),
    })
    public ResponseEntity<?> getPharmacyInfos(
            HttpServletRequest request,
            @Parameter(description = "UUID of the pharmacy information to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String id) {
        UUID uuid = UUID.fromString(id);
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<PharmacyInfos> pharmacyInfos = pharmacyInfosService.findPharmacyInfos(uuid, profil.getAccount());
        if (pharmacyInfos.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(pharmacyInfos.get());
    }

    @PostMapping
    @Operation(summary = "Create pharmacy information", description = "Creates a new pharmacy information entry for the authenticated user", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created pharmacy information", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyInfosDTO.class))),
            @ApiResponse(responseCode = "404", description = "Related pharmacy not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> createPharmacyInfos(
            HttpServletRequest request,
            @Parameter(description = "Pharmacy information creation data", required = true, schema = @Schema(implementation = PharmacyInfosCreateDTO.class)) @Valid @RequestBody PharmacyInfosCreateDTO pharmacyInfosCreateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<PharmacyInfos> optPharmacyInfos = pharmacyInfosService.createPharmacyInfos(profil,
                pharmacyInfosCreateDTO);
        if (optPharmacyInfos.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.created(pharmacyInfosMapper.toDto(optPharmacyInfos.get()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pharmacy information", description = "Deletes a specific pharmacy information entry", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted pharmacy information", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pharmacy information not found", content = @Content),
    })
    public ResponseEntity<?> deletePharmacyInfos(
            HttpServletRequest request,
            @Parameter(description = "UUID of the pharmacy information to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable @Valid @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Invalid UUID format") String id) {
        UUID uuid = UUID.fromString(id);
        Profil profil = (Profil) request.getAttribute("profil");
        pharmacyInfosService.deletePharmacyInfos(uuid, profil.getAccount());
        return MAPIR.deleted();
    }
}