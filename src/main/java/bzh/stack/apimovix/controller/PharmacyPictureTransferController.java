package bzh.stack.apimovix.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Picture.PharmacyPicture;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.repository.pharmacy.PharmacyInfosPictureRepository;
import bzh.stack.apimovix.service.pharmacy.PharmacyService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import bzh.stack.apimovix.util.PATTERNS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/pharmacy-picture-transfer", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@Tag(name = "Pharmacy Picture Transfer", description = "API pour transférer des photos de PharmacyInfos vers Pharmacy")
public class PharmacyPictureTransferController {

    private final PharmacyService pharmacyService;
    private final PharmacyInfosPictureRepository pharmacyInfosPictureRepository;

    @PostMapping("/{cip}/from-pharmacy-infos/{pictureId}")
    @Operation(summary = "Transférer une photo de PharmacyInfos vers Pharmacy",
               description = "Copie une photo depuis PharmacyInfosPicture vers PharmacyPicture pour une pharmacie donnée",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Photo transférée avec succès",
                               content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = PharmacyPicture.class))),
                   @ApiResponse(responseCode = "404", description = "Pharmacie ou photo PharmacyInfos non trouvée", content = @Content),
                   @ApiResponse(responseCode = "400", description = "La photo n'appartient pas à cette pharmacie", content = @Content),
                   @ApiResponse(responseCode = "500", description = "Erreur interne lors du transfert", content = @Content),
               })
    public ResponseEntity<?> transferPharmacyInfosPictureToPharmacy(
            HttpServletRequest request,
            @Parameter(description = "Code CIP de la pharmacie", required = true)
            @PathVariable String cip,
            @Parameter(description = "UUID de la photo PharmacyInfos à transférer", required = true,
                      schema = @Schema(type = "string", format = "uuid"))
            @PathVariable @Pattern(regexp = PATTERNS.UUID_PATTERN, message = "Format UUID invalide") String pictureId) {

        // Récupérer le profil de l'utilisateur connecté
        Profil profil = (Profil) request.getAttribute("profil");

        // Vérifier que la pharmacie existe (pass null for accountId as admin can access all)
        var optPharmacy = pharmacyService.findPharmacy(cip, null);
        if (optPharmacy.isEmpty()) {
            return MAPIR.notFound();
        }

        Pharmacy pharmacy = optPharmacy.get();
        UUID pharmacyInfosPictureId = UUID.fromString(pictureId);

        // Transférer la photo avec le compte de l'utilisateur
        PharmacyPicture transferredPicture = pharmacyService.copyPharmacyInfosPictureToPharmacy(pharmacy, pharmacyInfosPictureId, profil.getAccount());

        if (transferredPicture == null) {
            // Vérifier si c'est parce que la photo PharmacyInfos n'existe pas
            var optPharmacyInfosPicture = pharmacyInfosPictureRepository.findById(pharmacyInfosPictureId);
            if (optPharmacyInfosPicture.isEmpty()) {
                return MAPIR.notFound();
            }

            // Si la photo existe mais le transfert a échoué, c'est probablement une erreur interne
            return MAPIR.internalServerError();
        }

        return MAPIR.created(transferredPicture);
    }
} 