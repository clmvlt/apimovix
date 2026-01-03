package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.tour.TourAssignDTO;
import bzh.stack.apimovix.dto.tour.TourCreateDTO;
import bzh.stack.apimovix.dto.tour.TourDTO;
import bzh.stack.apimovix.dto.tour.TourDetailDTO;
import bzh.stack.apimovix.dto.tour.TourStatusDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateOrderDTO;
import bzh.stack.apimovix.dto.tour.TourUpdateStatusDTO;
import bzh.stack.apimovix.dto.tour.ValidateLoadingRequestDTO;
import bzh.stack.apimovix.dto.tour.ValidateLoadingResponseDTO;
import bzh.stack.apimovix.mapper.TourMapper;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.History.HistoryTourStatus;
import bzh.stack.apimovix.service.pdfGenerator.PdfGeneratorService;
import bzh.stack.apimovix.service.TarifService;
import bzh.stack.apimovix.service.tour.TourService;
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
@RequestMapping(value = "/tours", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@Tag(name = "Tours", description = "API for managing delivery tours and their lifecycle")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class TourController {
    private final TourService tourService;
    private final TarifService tarifService;
    private final TourMapper tourMapper;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/{id}")
    @Operation(summary = "Get tour by ID", description = "Retrieves detailed information about a specific tour", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tour details", content = @Content(schema = @Schema(implementation = TourDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    public ResponseEntity<?> getTour(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to retrieve", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Tour> optTour = tourService.findTour(profil.getAccount(), id);
        if (optTour.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(tourMapper.toDetailDto(optTour.get()));
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "Get tour history", description = "Retrieves the complete history of status changes for a specific tour", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tour history", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TourStatusDTO.class)))),
    })
    public ResponseEntity<?> getTourHistory(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to retrieve history for", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<HistoryTourStatus> tourHistory = tourService.findTourHistory(profil.getAccount(), id);
        return MAPIR.ok(tourMapper.toTourStatusDTOList(tourHistory));
    }

    @GetMapping("/by-date/{date}")
    @Operation(summary = "Get tours by date", description = "Retrieves all tours scheduled for a specific date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tours", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TourDTO.class)))),
    })
    public ResponseEntity<?> getToursByDate(
            HttpServletRequest request,
            @Parameter(description = "Date to retrieve tours for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @PathVariable String date) {
        Profil profil = (Profil) request.getAttribute("profil");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERNS.DATE);
        LocalDate localDate = LocalDate.parse(date, formatter);
        List<Tour> tours = tourService.findTours(profil.getAccount(), localDate);
        List<TourDTO> tourDTOs = tours.stream()
                .map(tourMapper::toDto)
                .collect(Collectors.toList());
        return MAPIR.ok(tourDTOs);
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get tours by date range", description = "Retrieves all tours scheduled between a start date and end date", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tours", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TourDTO.class)))),
    })
    public ResponseEntity<?> getToursByDateRange(
            HttpServletRequest request,
            @Parameter(description = "Start date to retrieve tours for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @RequestParam String startDate,
            @Parameter(description = "End date to retrieve tours for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @RequestParam String endDate) {
        Profil profil = (Profil) request.getAttribute("profil");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERNS.DATE);
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);
        List<Tour> tours = tourService.findToursByDateRange(profil.getAccount(), startLocalDate, endLocalDate);
        List<TourDTO> tourDTOs = tours.stream()
                .map(tourMapper::toDto)
                .peek(tourDTO -> tourDTO.setGeometry(""))
                .collect(Collectors.toList());
        return MAPIR.ok(tourDTOs);
    }

    @GetMapping("/by-profile")
    @Operation(summary = "Get tours by profile", description = "Retrieves all tours assigned to the authenticated user's profile filtred only création,chargement,livraison", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tours", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TourDTO.class)))),
    })
    @MobileRequired
    public ResponseEntity<?> getToursByProfile(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Tour> tours = tourService.findToursByProfile(profil.getAccount(), profil);
        List<TourDetailDTO> tourDTOs = tours.stream()
                .map(tourMapper::toDetailDto)
                .collect(Collectors.toList());
        return MAPIR.ok(tourDTOs);
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Update tour", description = "Updates the information of an existing tour", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated tour", content = @Content(schema = @Schema(implementation = TourUpdateDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updateTour(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to update", required = true) @PathVariable String id,
            @Parameter(description = "Updated tour information", required = true, schema = @Schema(implementation = TourUpdateDTO.class)) @RequestBody TourUpdateDTO tourUpdate) {
        Profil profil = (Profil) request.getAttribute("profil");

        // Si l'utilisateur a seulement le rôle mobile, vérifier qu'il est assigné à la tournée
        if (isMobileOnly(profil)) {
            Optional<Tour> optTour = tourService.findTour(profil.getAccount(), id);
            if (optTour.isEmpty()) {
                return MAPIR.notFound();
            }
            if (!isAssignedToTour(profil, optTour.get())) {
                return MAPIR.forbidden();
            }
        }

        Optional<Tour> optEditedTour = tourService.updateTour(profil.getAccount(), id, tourUpdate, profil);
        if (optEditedTour.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.ok(tourMapper.toDto(optEditedTour.get()));
    }

    @PutMapping(value = "/state")
    @Operation(summary = "Update tour state", description = "Updates the state/status of one or more tours in bulk", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated tour state(s)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour(s) not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updateTourState(
            HttpServletRequest request,
            @Parameter(description = "Tour status update data", required = true, schema = @Schema(implementation = TourUpdateStatusDTO.class)) @Valid @RequestBody TourUpdateStatusDTO tourStatusDTO) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean updated = tourService.updateTourStatusBulk(profil, tourStatusDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PutMapping(value = "/assign/{id}")
    @Operation(summary = "Assign tour", description = "Assigns a tour to a specific profile or the authenticated user", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully assigned tour", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> assignTour(
            HttpServletRequest request,
            @Parameter(description = "Tour assignment data", required = true, schema = @Schema(implementation = TourAssignDTO.class)) @Valid @RequestBody TourAssignDTO tourAssignDTO,
            @Parameter(description = "ID of the tour to assign", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Tour> optTour;

        if (tourAssignDTO.getProfilId() == null) {
            optTour = tourService.assignTour(profil.getAccount(), id, profil);
        } else {
            if (profil.getIsWeb()) {
                optTour = tourService.assignTour(profil.getAccount(), id, tourAssignDTO.getProfilId());
            } else {
                return MAPIR.forbidden();
            }
        }
        if (optTour.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PostMapping(value = "/validate-loading/{id}")
    @Operation(summary = "Validate tour loading", description = "Validates the loading of packages for a specific tour", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully validated tour loading", content = @Content(schema = @Schema(implementation = ValidateLoadingResponseDTO.class))),
            @ApiResponse(responseCode = "207", description = "Multi-status - Some validations failed", content = @Content(schema = @Schema(implementation = ValidateLoadingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> validateLoading(
            HttpServletRequest request,
            @Parameter(description = "Loading validation data", required = true, schema = @Schema(implementation = ValidateLoadingRequestDTO.class)) @Valid @RequestBody ValidateLoadingRequestDTO validateLoadingRequestDTO,
            @Parameter(description = "ID of the tour to validate loading for", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");

        Optional<ValidateLoadingResponseDTO> response = tourService.validateLoading(profil, validateLoadingRequestDTO,
                id);

        if (response.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.created(response);
    }

    @PutMapping(value = "/update-order/{id}")
    @Operation(summary = "Update tour order", description = "Updates the order of stops in a tour", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated tour order", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updateOrder(
            HttpServletRequest request,
            @Parameter(description = "Tour order update data", required = true, schema = @Schema(implementation = TourUpdateOrderDTO.class)) @Valid @RequestBody TourUpdateOrderDTO tourUpdateOrderDTO,
            @Parameter(description = "ID of the tour to update order for", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");

        // Si l'utilisateur a seulement le rôle mobile, vérifier qu'il est assigné à la tournée
        if (isMobileOnly(profil)) {
            Optional<Tour> optTour = tourService.findTour(profil.getAccount(), id);
            if (optTour.isEmpty()) {
                return MAPIR.notFound();
            }
            if (!isAssignedToTour(profil, optTour.get())) {
                return MAPIR.forbidden();
            }
        }

        boolean updated = tourService.updateTourOrder(profil.getAccount(), tourUpdateOrderDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PostMapping
    @Operation(summary = "Create tour", description = "Creates a new tour with the provided information", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created tour", content = @Content(schema = @Schema(implementation = TourDTO.class))),
    })
    public ResponseEntity<?> createTour(
            HttpServletRequest request,
            @Parameter(description = "Tour creation data", required = true, schema = @Schema(implementation = TourCreateDTO.class)) @RequestBody @Valid TourCreateDTO tourCreateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");

        Tour createdTour = tourService.createTour(profil, tourCreateDTO);

        return MAPIR.created(tourMapper.toDto(createdTour));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tour", description = "Deletes a specific tour", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted tour", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    public ResponseEntity<?> deleteTour(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to delete", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean deleted = tourService.deleteTour(profil.getAccount(), id);
        if (!deleted) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PutMapping(value = "/unassign/{id}")
    @Operation(summary = "Unassign tour", description = "Removes the assignment of a tour from a profile", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully unassigned tour", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> unassignTour(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to unassign", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Tour> optTour = tourService.unassignTour(profil.getAccount(), id);
        
        if (optTour.isEmpty()) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PostMapping(value = "/pdf/{id}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get tour PDF", description = "Generates a PDF document for a specific tour", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated tour PDF", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating PDF", content = @Content)
    })
    public ResponseEntity<?> getPDF(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to generate PDF for", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Tour> tour = tourService.findTour(profil.getAccount(), id);
        if (tour.isEmpty()) {
            return MAPIR.notFound();
        }

        try {
            byte[] pdfBytes = pdfGeneratorService.generateTourPdf(tour.get());
            return MAPIR.pdf(pdfBytes, id + ".pdf");
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }

    @PostMapping(value = "/pdf-tarif/{id}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get tour PDF", description = "Generates a Tarif PDF document for a specific tour", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated tour Tarif PDF", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Tour not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating PDF", content = @Content)
    })
    @AdminRequired
    public ResponseEntity<?> getTarifPDF(
            HttpServletRequest request,
            @Parameter(description = "ID of the tour to generate PDF for", required = true) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Tour> tour = tourService.findTourForTarif(profil.getAccount(), id);
        if (tour.isEmpty()) {
            return MAPIR.notFound();
        }
        List<Tarif> tarifs = tarifService.findTarifsByAccount(profil.getAccount());

        try {
            byte[] pdfBytes = pdfGeneratorService.generateTarifTourPdf(tour.get(), tarifs);
            return MAPIR.pdf(pdfBytes, id + ".pdf");
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }

    /**
     * Vérifie si l'utilisateur a uniquement le rôle mobile (pas web ni admin)
     */
    private boolean isMobileOnly(Profil profil) {
        boolean isWeb = Boolean.TRUE.equals(profil.getIsWeb());
        boolean isAdmin = Boolean.TRUE.equals(profil.getIsAdmin());
        return !isWeb && !isAdmin;
    }

    /**
     * Vérifie si le profil est assigné à la tournée
     */
    private boolean isAssignedToTour(Profil profil, Tour tour) {
        return tour.getProfil() != null && tour.getProfil().getId().equals(profil.getId());
    }
}
