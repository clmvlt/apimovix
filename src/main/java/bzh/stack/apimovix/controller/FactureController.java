package bzh.stack.apimovix.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.facture.FactureCreateDTO;
import bzh.stack.apimovix.dto.facture.FactureDTO;
import bzh.stack.apimovix.dto.facture.FactureUpdateDTO;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.FactureService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@TokenRequired
@RequestMapping(value = "/factures", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Facture", description = "API for invoice management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping
    @Operation(summary = "Get factures for account", description = "Gets all factures for the connected user's account ordered by date descending (admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved factures", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FactureDTO.class)))
    })
    @AdminRequired
    public ResponseEntity<?> getFactures(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            List<FactureDTO> factures = factureService.getFacturesByAccountId(profil.getAccount().getId());
            return MAPIR.ok(factures);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @GetMapping(value = "/pdf/{factureId}", produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Download facture PDF", description = "Downloads the PDF file for a specific facture (admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "PDF file downloaded successfully", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE))
    })
    @AdminRequired
    public ResponseEntity<?> downloadPdf(
            @Parameter(description = "Facture ID", required = true) @PathVariable UUID factureId,
            HttpServletRequest request) {

        Profil profil = (Profil) request.getAttribute("profil");
        if (profil == null || profil.getAccount() == null) {
            return MAPIR.invalidCredentials();
        }

        try {
            // Verify the facture belongs to the user's account
            var facture = factureService.getFactureById(factureId);
            if (!facture.getAccount().getId().equals(profil.getAccount().getId())) {
                return MAPIR.forbidden();
            }

            File pdfFile = factureService.getPdfFile(factureId);
            Resource resource = new FileSystemResource(pdfFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFile.getName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    // ==================== HYPERADMIN ROUTES ====================

    @PostMapping
    @Operation(summary = "Create facture", description = "Creates a new facture for an account with PDF in base64 (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully created facture", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FactureDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> createFacture(
            @Parameter(description = "Facture data with PDF in base64", required = true) @RequestBody FactureCreateDTO createDTO) {

        try {
            if (createDTO.getPdfBase64() == null || createDTO.getPdfBase64().isEmpty()) {
                return MAPIR.badRequest("Le fichier PDF en base64 est requis");
            }

            if (createDTO.getAccountId() == null) {
                return MAPIR.badRequest("L'ID du compte est requis");
            }

            if (createDTO.getDateFacture() == null) {
                return MAPIR.badRequest("La date de la facture est requise");
            }

            if (createDTO.getMontantTTC() == null) {
                return MAPIR.badRequest("Le montant TTC est requis");
            }

            FactureDTO facture = factureService.createFacture(createDTO);
            return MAPIR.ok(facture);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @PutMapping("/{factureId}")
    @Operation(summary = "Update facture", description = "Updates an existing facture by its ID (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated facture", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FactureDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> updateFacture(
            @Parameter(description = "Facture ID", required = true) @PathVariable UUID factureId,
            @Parameter(description = "Facture update data", required = true) @RequestBody FactureUpdateDTO updateDTO) {

        try {
            FactureDTO facture = factureService.updateFacture(factureId, updateDTO);
            return MAPIR.ok(facture);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/{factureId}")
    @Operation(summary = "Delete facture", description = "Deletes a facture by its ID (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted facture")
    })
    @HyperAdminRequired
    public ResponseEntity<?> deleteFacture(
            @Parameter(description = "Facture ID", required = true) @PathVariable UUID factureId) {

        try {
            factureService.deleteFacture(factureId);
            return MAPIR.ok("Facture supprimée avec succès");
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get factures by account ID", description = "Gets all factures for a specific account (hyper admin required)", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved factures", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FactureDTO.class)))
    })
    @HyperAdminRequired
    public ResponseEntity<?> getFacturesByAccountId(
            @Parameter(description = "Account ID", required = true) @PathVariable UUID accountId) {

        try {
            List<FactureDTO> factures = factureService.getFacturesByAccountId(accountId);
            return MAPIR.ok(factures);
        } catch (RuntimeException e) {
            return MAPIR.badRequest(e.getMessage());
        }
    }
}
