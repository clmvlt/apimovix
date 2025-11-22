package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.common.PictureDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyCreateDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyDetailDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacySearchDTO;
import bzh.stack.apimovix.dto.pharmacy.PharmacyUpdateDTO;
import bzh.stack.apimovix.dto.tour.PharmacyOrderStatsDTO;
import bzh.stack.apimovix.mapper.PharmacyMapper;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
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
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@TokenRequired
@RequestMapping(value = "/pharmacies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pharmacies", description = "API for managing pharmacies and their information")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class PharmacyController {
    private final PharmacyService pharmacyService;
    private final TourService tourService;
    private final PharmacyMapper pharmacyMapper;
    private final bzh.stack.apimovix.service.PdfGeneratorService pdfGeneratorService;

    @GetMapping
    @Operation(summary = "Get all pharmacies", description = "Retrieves a list of all pharmacies belonging to the authenticated account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of pharmacies", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PharmacyDTO.class)))),
    })
    public ResponseEntity<?> getAllPharmacies(ServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Pharmacy> pharmacies = pharmacyService.findPharmaciesByAccount(profil.getAccount());
        List<PharmacyDTO> pharmacyDTOs = pharmacies.stream()
                .map(pharmacyMapper::toDto)
                .toList();
        return MAPIR.ok(pharmacyDTOs);
    }

    @HyperAdminRequired
    @GetMapping("/admin/{cip}")
    @Operation(summary = "Get pharmacy by CIP (HyperAdmin only)", description = "Retrieves detailed information about a specific pharmacy using its CIP code without account restriction. This operation requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content),
    })
    public ResponseEntity<?> getPharmacyAdmin(
            @Parameter(description = "CIP code of the pharmacy to retrieve", required = true) @PathVariable String cip) {
        // Admin can access all pictures, pass null for accountId
        Optional<Pharmacy> pharmacy = pharmacyService.findPharmacy(cip, null);
        if (pharmacy.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(pharmacyMapper.toDetailDto(pharmacy.get()));
    }

    @MobileRequired
    @GetMapping("/{cip}")
    @Operation(summary = "Get pharmacy by CIP", description = "Retrieves detailed information about a specific pharmacy using its CIP code, filtered by account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found or doesn't belong to your account", content = @Content),
    })
    public ResponseEntity<?> getPharmacy(
            ServletRequest request,
            @Parameter(description = "CIP code of the pharmacy to retrieve", required = true) @PathVariable String cip) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Pharmacy> pharmacy = pharmacyService.findPharmacyByAccount(profil.getAccount(), cip);
        if (pharmacy.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(pharmacyMapper.toDetailDto(pharmacy.get()));
    }

    @PostMapping("/{cip}/picture")
    @Operation(summary = "Upload pharmacy picture", description = "Uploads a picture associated with a specific pharmacy, filtered by account", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully uploaded pharmacy picture", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyPicture.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found or doesn't belong to your account", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while processing picture", content = @Content),
    })
    public ResponseEntity<?> uploadPicture(
            ServletRequest request,
            @Parameter(description = "CIP code of the pharmacy to upload picture for", required = true) @PathVariable String cip,
            @Parameter(description = "Picture data in base64 format", required = true, schema = @Schema(implementation = PictureDTO.class)) @RequestBody PictureDTO body) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Pharmacy> pharmacy = pharmacyService.findPharmacyByAccount(profil.getAccount(), cip);
        if (pharmacy.isEmpty()) {
            return MAPIR.notFound();
        }

        PharmacyPicture picture = pharmacyService.createPharmacyPicture(pharmacy.get(), profil.getAccount(), body.getBase64());

        if (picture == null) {
            return MAPIR.internalServerError();
        }

        return MAPIR.created(picture);
    }

    @DeleteMapping("/{cip}/picture/{id}")
    @Operation(summary = "Delete pharmacy picture", description = "Deletes a specific picture associated with a pharmacy, filtered by account", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted pharmacy picture", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pharmacy or picture not found, or doesn't belong to your account", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting picture", content = @Content),
    })
    public ResponseEntity<?> deletePicture(
            ServletRequest request,
            @Parameter(description = "CIP code of the pharmacy", required = true) @PathVariable String cip,
            @Parameter(description = "UUID of the picture to delete", required = true, schema = @Schema(type = "string", format = "uuid")) @PathVariable String id) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Pharmacy> pharmacy = pharmacyService.findPharmacyByAccount(profil.getAccount(), cip);
        if (pharmacy.isEmpty()) {
            return MAPIR.notFound();
        }

        UUID uuid = UUID.fromString(id);
        Boolean deleted = pharmacyService.deletePharmacyPhoto(pharmacy.get(), profil.getAccount(), uuid);

        if (deleted) {
            return MAPIR.deleted();
        }

        return MAPIR.internalServerError();
    }

    @PostMapping("/search")
    @Operation(summary = "Search pharmacies", description = "Searches for pharmacies based on provided criteria, filtered by account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching pharmacies", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PharmacyDTO.class)))),
    })
    public ResponseEntity<?> searchPharmacy(
            ServletRequest request,
            @Parameter(description = "Search criteria for finding pharmacies", required = true, schema = @Schema(implementation = PharmacySearchDTO.class)) @RequestBody PharmacySearchDTO pharmacySearchDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<Pharmacy> pharmacies = pharmacyService.searchPharmaciesByAccount(profil.getAccount(), pharmacySearchDTO);
        List<PharmacyDTO> pharmacyDTOs = pharmacies.stream()
                .map(pharmacyMapper::toDto)
                .toList();
        return MAPIR.ok(pharmacyDTOs);
    }

    @PostMapping
    @Operation(summary = "Create pharmacy", description = "Creates a new pharmacy with the provided information", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created pharmacy", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyDTO.class))),
    })
    public ResponseEntity<?> createPharmacy(
            ServletRequest request,
            @Parameter(description = "Pharmacy creation data", required = true, schema = @Schema(implementation = PharmacyCreateDTO.class)) @Valid @RequestBody PharmacyCreateDTO pharmacyCreateDTO) {
        Profil profil = (Profil) request.getAttribute("profil");
        Pharmacy pharmacy = pharmacyService.createPharmacy(profil.getAccount(), pharmacyCreateDTO);
        return MAPIR.created(pharmacyMapper.toDto(pharmacy));
    }

    @PutMapping("/{cip}")
    @Operation(summary = "Update pharmacy", description = "Updates the information of an existing pharmacy", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully updated pharmacy", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PharmacyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content),
    })
    public ResponseEntity<?> updatePharmacy(ServletRequest request, 
            @Parameter(description = "Updated pharmacy information", required = true, schema = @Schema(implementation = PharmacyUpdateDTO.class)) @RequestBody PharmacyUpdateDTO pharmacyUpdateDTO,
            @Parameter(description = "CIP code of the pharmacy to update", required = true) @PathVariable String cip) {
                Profil profil = (Profil) request.getAttribute("profil");
        Optional<Pharmacy> optPharmacy = pharmacyService.updatePharmacy(profil.getAccount(), pharmacyUpdateDTO, cip);
        if (optPharmacy.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.created(pharmacyMapper.toDto(optPharmacy.get()));
    }

    @GetMapping("/{cip}/exists")
    @Operation(summary = "Check if pharmacy exists", description = "Verifies if a pharmacy exists with the given CIP code for the authenticated account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully checked pharmacy existence", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
    })
    public ResponseEntity<?> checkPharmacyExists(
            ServletRequest request,
            @Parameter(description = "CIP code of the pharmacy to check", required = true) @PathVariable String cip) {
        Profil profil = (Profil) request.getAttribute("profil");
        boolean exists = pharmacyService.findPharmacyByAccount(profil.getAccount(), cip).isPresent();
        return MAPIR.ok(exists);
    }

    @GetMapping("/exist/{cip}")
    @Operation(summary = "Check if pharmacy CIP exists globally", description = "Verifies if a pharmacy exists with the given CIP code across all pharmacies in the system, regardless of account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully checked pharmacy CIP existence", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
    })
    public ResponseEntity<?> checkCipExistsGlobally(
            @Parameter(description = "CIP code of the pharmacy to check", required = true) @PathVariable String cip) {
        boolean exists = pharmacyService.checkCipExists(cip);
        return MAPIR.ok(exists);
    }

    @HyperAdminRequired
    @DeleteMapping("/{cip}")
    @Operation(summary = "Delete pharmacy (HyperAdmin only)", description = "Deletes a specific pharmacy and all its associated data. This operation requires HyperAdmin privileges. Related commands and anomalies will have their pharmacy reference set to null.", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted pharmacy", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting pharmacy", content = @Content)
    })
    public ResponseEntity<?> deletePharmacy(
            @Parameter(description = "CIP code of the pharmacy to delete", required = true) @PathVariable String cip) {
        try {
            // HyperAdmin can delete all pictures from all accounts, so pass null
            boolean deleted = pharmacyService.deletePharmacy(cip, null);
            if (!deleted) {
                return MAPIR.notFound();
            }
            return MAPIR.deleted();
        } catch (Exception e) {
            return MAPIR.internalServerError();
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get pharmacy order statistics by date range", description = "Retrieves the number of orders per pharmacy for tours within a date range", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy statistics", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PharmacyOrderStatsDTO.class)))),
    })
    public ResponseEntity<?> getPharmacyOrderStats(
            HttpServletRequest request,
            @Parameter(description = "Start date to retrieve statistics for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @RequestParam String startDate,
            @Parameter(description = "End date to retrieve statistics for (format: yyyy-MM-dd)", required = true, schema = @Schema(type = "string", format = "date")) @Pattern(regexp = PATTERNS.REG_DATE, message = GLOBAL.PATH_INVALID_FORMAT_DATE) @RequestParam String endDate) {
        Profil profil = (Profil) request.getAttribute("profil");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERNS.DATE);
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);
        List<PharmacyOrderStatsDTO> stats = tourService.getPharmacyOrderStats(profil.getAccount(), startLocalDate, endLocalDate);
        return MAPIR.ok(stats);
    }

    @PostMapping(value = "/{cip}/label", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Generate pharmacy label with barcode", description = "Generates a PDF label with the account logo, pharmacy name and CIP barcode, filtered by account", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated pharmacy label", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found or doesn't belong to your account", content = @Content),
            @ApiResponse(responseCode = "406", description = "Not Acceptable - Client must accept application/pdf", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating label", content = @Content)
    })
    public ResponseEntity<?> generatePharmacyLabel(
            ServletRequest request,
            @Parameter(description = "CIP code of the pharmacy", required = true) @PathVariable String cip) {
        Profil profil = (Profil) request.getAttribute("profil");
        Optional<Pharmacy> pharmacy = pharmacyService.findPharmacyByAccount(profil.getAccount(), cip);
        if (pharmacy.isEmpty()) {
            return MAPIR.notFound();
        }

        try {
            byte[] pdfBytes = pdfGeneratorService.generatePharmacyLabel(pharmacy.get(), profil.getAccount());
            return MAPIR.pdf(pdfBytes, "pharmacy-label-" + cip + ".pdf");
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }
}