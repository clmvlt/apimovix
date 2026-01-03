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
import bzh.stack.apimovix.model.ImporterToken;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.service.ImporterService;
import bzh.stack.apimovix.service.pdfGenerator.PdfGeneratorService;
import bzh.stack.apimovix.service.packageservices.PackageService;
import bzh.stack.apimovix.util.MAPIR;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Importer", description = "API d'import de commandes vers le système Movix.")
@RequestMapping("/")
@ApiResponse(responseCode = "400", description = "Données d'entrée invalides", content = @Content)
@ApiResponse(responseCode = "401", description = "Non autorisé - Token d'import manquant ou invalide", content = @Content())
@ApiResponse(responseCode = "403", description = "Accès refusé - Token d'import non autorisé pour cette action", content = @Content())
@ImporterRequired
public class ImporterController {

    @Autowired
    ImporterService importerService;
    @Autowired
    PackageService packageService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/command/send")
    @Operation(
        summary = "Envoyer une commande",
        description = """
            Permet d'envoyer une nouvelle commande au système Movix.

            **Fonctionnement :**
            - Si l'expéditeur (sender) n'existe pas, il sera créé automatiquement
            - Si le destinataire (recipient) n'existe pas, il sera créé automatiquement
            - Chaque colis (package) reçoit un code-barres unique et une URL d'étiquette
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Commande créée avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SendCommandResponseDTO.class))),
        }
    )
    public ResponseEntity<?> sendCommand(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de la commande à envoyer",
                required = true
            )
            @Valid @RequestBody SendCommandRequestDTO body,
            HttpServletRequest request) {

        // Vérifier que le sender.code correspond au expCode du token
        ImporterToken importerToken = (ImporterToken) request.getAttribute("importerToken");
        if (importerToken != null && importerToken.getExpCode() != null && !importerToken.getExpCode().isEmpty()) {
            String senderCode = body.getSender() != null ? body.getSender().getCode() : null;
            if (senderCode == null || !senderCode.equals(importerToken.getExpCode())) {
                return MAPIR.forbidden("Le code expéditeur ne correspond pas au token utilisé");
            }
        }

        SendCommandResponseDTO responseDTO = importerService.sendCommand(body);
        return MAPIR.ok(responseDTO);
    }

    @PostMapping(value = "/package/getLabel/{barcode}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(
        summary = "Obtenir l'étiquette d'un colis",
        description = """
            Génère et retourne l'étiquette PDF d'un colis à partir de son code-barres.

            **Utilisation :**
            - Le code-barres correspond à l'identifiant unique du colis (champ `id` du package lors de l'envoi)
            - Le fichier PDF retourné peut être directement imprimé

            **Format de réponse :**
            - Succès : Fichier PDF (application/pdf)
            - Erreur : JSON avec message d'erreur
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Étiquette PDF générée avec succès", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Colis non trouvé - Le code-barres ne correspond à aucun colis", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de la génération du PDF", content = @Content),
        }
    )
    public ResponseEntity<?> getLabel(
            @Parameter(description = "Code-barres du colis (identifiant unique)", required = true, example = "340002200000299670") @PathVariable String barcode) {
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
