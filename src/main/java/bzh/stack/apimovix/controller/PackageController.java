package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
import bzh.stack.apimovix.dto.packageentity.PackageChangeCommandDTO;
import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.packageentity.PackageStatusDTO;
import bzh.stack.apimovix.dto.packageentity.PackageUpdateStatusDTO;
import bzh.stack.apimovix.mapper.PackageMapper;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;
import bzh.stack.apimovix.service.PdfGeneratorService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/packages", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@Tag(name = "Packages", description = "API for managing packages and their statuses")
public class PackageController {

    private final PackageMapper packageMapper;
    private final PdfGeneratorService pdfGeneratorService;
    private final PackageService packageService;

    @GetMapping
    @Operation(summary = "Search packages by barcode pattern (HyperAdmin only)", description = "Retrieves all packages containing the specified barcode pattern. Requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved packages", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PackageDTO.class)))),
    })
    @HyperAdminRequired
    public ResponseEntity<?> searchPackages(
            @Parameter(description = "Barcode pattern to search for (searches for packages containing this string)", required = true) @RequestParam String barcode) {
        List<PackageEntity> packages = packageService.findPackagesByBarcodePattern(barcode);
        return MAPIR.ok(packages.stream()
                .map(packageMapper::toDto)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/history/{barcode}")
    @Operation(summary = "Get package history", description = "Retrieves the complete history of status changes for a specific package using its barcode", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved package history", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PackageStatusDTO.class)))),
    })
    public ResponseEntity<?> getPackageHistory(
            HttpServletRequest request,
            @Parameter(description = "Barcode of the package to retrieve history for", required = true) @PathVariable String barcode) {
        Profil profil = (Profil) request.getAttribute("profil");
        List<HistoryPackageStatus> packageHistory = packageService.findPackageHistory(profil.getAccount(), barcode);
        return MAPIR.ok(packageMapper.toPackageStatusDTOList(packageHistory));
    }

    @PutMapping("/state")
    @Operation(summary = "Update package state", description = "Updates the state/status of one or more packages in bulk", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully updated package state(s)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Package(s) not found", content = @Content),
    })
    @MobileRequired
    public ResponseEntity<?> updatePackageState(
            HttpServletRequest request,
            @Parameter(description = "Package status update data", required = true, schema = @Schema(implementation = PackageUpdateStatusDTO.class)) @Valid @RequestBody PackageUpdateStatusDTO packageUpdateStatusDTO) {
        Profil profil = (Profil) request.getAttribute("profil");

        boolean updated = packageService.updatePackageStatusBulk(profil, packageUpdateStatusDTO);
        if (!updated) {
            return MAPIR.notFound();
        }

        return MAPIR.noContent();
    }

    @PostMapping(value = "/label/{barcode}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Generate package label", description = "Generates a PDF label for a specific package using its barcode", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated package label", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Package not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Not Acceptable - Client must accept application/pdf", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating PDF", content = @Content)
    })
    public ResponseEntity<?> getLabel(
            @Parameter(description = "Barcode of the package to generate label for", required = true) @PathVariable String barcode) {
        Optional<PackageEntity> packageEntity = packageService.findPackage(barcode);
        if (packageEntity.isEmpty()) {
            return MAPIR.notFound();
        }

        try {
            byte[] pdfBytes = pdfGeneratorService.generateLabel(packageEntity.get());
            return MAPIR.pdf(pdfBytes, barcode + ".pdf");
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }

    @DeleteMapping("/{barcode}")
    @Operation(summary = "Delete package (HyperAdmin only)", description = "Deletes a package and all its related data including history. This operation is irreversible and requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "204", description = "Package successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Package not found", content = @Content),
    })
    @HyperAdminRequired
    public ResponseEntity<?> deletePackage(
            @Parameter(description = "Barcode of the package to delete", required = true) @PathVariable String barcode) {
        boolean deleted = packageService.deletePackage(barcode);
        if (!deleted) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @PutMapping("/{barcode}/command")
    @Operation(summary = "Change package command (HyperAdmin only)", description = "Changes the command (id_command) associated with a package. Requires HyperAdmin privileges.", responses = {
            @ApiResponse(responseCode = "204", description = "Package command successfully updated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Package or command not found", content = @Content),
    })
    @HyperAdminRequired
    public ResponseEntity<?> changePackageCommand(
            @Parameter(description = "Barcode of the package to update", required = true) @PathVariable String barcode,
            @Parameter(description = "New command ID data", required = true, schema = @Schema(implementation = PackageChangeCommandDTO.class)) @Valid @RequestBody PackageChangeCommandDTO dto) {
        boolean updated = packageService.changePackageCommand(barcode, dto.getNewCommandId());
        if (!updated) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }
}