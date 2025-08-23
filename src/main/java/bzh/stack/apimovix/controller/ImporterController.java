package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.ImporterRequired;
import bzh.stack.apimovix.dto.importer.SendCommandRequestDTO;
import bzh.stack.apimovix.dto.importer.SendCommandResponseDTO;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.service.ImporterService;
import bzh.stack.apimovix.service.PdfGeneratorService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Importer", description = "API for importing external data into the Movix system")
@RequestMapping("/")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content())
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content())
@ImporterRequired
public class ImporterController {

    @Autowired
    ImporterService importerService;
    @Autowired
    PackageService packageService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/command/send")
    @Operation(summary = "Send command", description = "Sends a command to the system for processing", responses = {
            @ApiResponse(responseCode = "200", description = "Command successfully sent", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SendCommandResponseDTO.class))),
    })
    public ResponseEntity<?> sendCommand(
            @Parameter(description = "Command data to be sent", required = true, schema = @Schema(implementation = SendCommandRequestDTO.class)) @Valid @RequestBody SendCommandRequestDTO body) {
        SendCommandResponseDTO responseDTO = importerService.sendCommand(body);
        return MAPIR.ok(responseDTO);
    }

    @PostMapping(value = "/package/getLabel/{barcode}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get package label", description = "Generates a PDF label for a specific package using its barcode", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully generated package label", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Package not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating PDF", content = @Content),
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
}
