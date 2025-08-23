package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.anomalie.AnomalieCreateDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieDetailDTO;
import bzh.stack.apimovix.dto.anomalie.AnomalieSearchDTO;
import bzh.stack.apimovix.mapper.AnomalieMapper;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.AnomalieService;
import bzh.stack.apimovix.service.PdfGeneratorService;
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
@RequestMapping(value = "/anomalies", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Anomalies", description = "API for managing anomalies in the system")
@TokenRequired
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
public class AnomalieController {
    private final AnomalieService anomalieService;
    private final AnomalieMapper anomalieMapper;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping
    @Operation(summary = "Get all anomalies", description = "Retrieves a list of all anomalies associated with the authenticated user's account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of anomalies", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AnomalieDTO.class)))),
    })
    public ResponseEntity<?> getAnomalies(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Anomalie> anomalies = anomalieService.findAnomalies(profil.getAccount());
        return MAPIR.ok(anomalieMapper.toAnomalieDTOsList(anomalies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get anomaly by ID", description = "Retrieves detailed information about a specific anomaly using its UUID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved anomaly details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AnomalieDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anomaly not found", content = @Content),
    })
    public ResponseEntity<?> getAnomalie(
            HttpServletRequest request,
            @Parameter(description = "UUID of the anomaly to retrieve", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Anomalie> optAnomalie = anomalieService.findAnomalie(profil.getAccount(), uuid);
        if (optAnomalie.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.ok(anomalieMapper.toDetailDto(optAnomalie.get()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search anomalies with filters", description = "Search anomalies with optional filters for user, date range, pharmacy, and type", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered anomalies", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = AnomalieDTO.class)))),
    })
    public ResponseEntity<?> searchAnomalies(
            HttpServletRequest request,
            @Parameter(description = "Search criteria for anomalies", required = true, schema = @Schema(implementation = AnomalieSearchDTO.class)) @Valid @RequestBody AnomalieSearchDTO searchDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Anomalie> anomalies = anomalieService.searchAnomalies(profil.getAccount(), searchDTO);
        return MAPIR.ok(anomalieMapper.toAnomalieDTOsList(anomalies));
    }

    @PostMapping
    @Operation(summary = "Create new anomaly", description = "Creates a new anomaly with the provided information", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created anomaly", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AnomalieDetailDTO.class))),
    })
    @MobileRequired
    public ResponseEntity<?> createAnomalie(
            HttpServletRequest request,
            @Parameter(description = "Anomaly creation data", required = true, schema = @Schema(implementation = AnomalieCreateDTO.class)) @Valid @RequestBody AnomalieCreateDTO anomalieCreateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Anomalie> optAnomalie = anomalieService.createAnomalie(profil, anomalieCreateDTO);

        if (optAnomalie.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.created(anomalieMapper.toDetailDto(optAnomalie.get()));
    }

    @PostMapping(value = "/{id}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Generate anomaly PDF", description = "Generates a PDF document for a specific anomaly using the template", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated PDF", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Anomaly not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error generating PDF", content = @Content),
    })
    public ResponseEntity<?> generateAnomaliePdf(
            HttpServletRequest request,
            @Parameter(description = "UUID of the anomaly to generate PDF for", required = true, schema = @Schema(type = "string", format = "uuid")) @Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.PATH_INVALID_FORMAT_UUID) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        UUID uuid = UUID.fromString(id);
        Optional<Anomalie> optAnomalie = anomalieService.findAnomalie(profil.getAccount(), uuid);
        
        if (optAnomalie.isEmpty()) {
            return MAPIR.notFound();
        }

        try {
            byte[] pdfBytes = pdfGeneratorService.generateAnomaliePdf(optAnomalie.get());
            String filename = "anomalie_" + uuid + ".pdf";
            return MAPIR.pdf(pdfBytes, filename);
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }
}
