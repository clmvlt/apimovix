package bzh.stack.apimovix.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "API for managing and retrieving images across the application")
public class ImageController {
    private final PictureService pictureService;

    @GetMapping("/pharmacy/{cip}/{imageId}")
    @Operation(summary = "Get pharmacy image", description = "Retrieves an image associated with a specific pharmacy using CIP code and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Image successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
    })
    public ResponseEntity<?> getPharmacyImage(
            @Parameter(description = "CIP code of the pharmacy", required = true) @PathVariable String cip,
            @Parameter(description = "ID of the image to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("pharmacy/%s/%s", cip, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping("/pharmacyinfos/{cip}/{pharmacyInfosId}/{imageId}")
    @Operation(summary = "Get pharmacy information image", description = "Retrieves an image associated with specific pharmacy information using CIP code, pharmacy info ID and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Image successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
    })
    public ResponseEntity<?> getPharmacyInfosImage(
            @Parameter(description = "CIP code of the pharmacy", required = true) @PathVariable String cip,
            @Parameter(description = "ID of the pharmacy information", required = true) @PathVariable String pharmacyInfosId,
            @Parameter(description = "ID of the image to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("pharmacyinfos/%s/%s/%s", cip, pharmacyInfosId, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping("/command/{y}/{m}/{d}/{commandId}/{imageId}")
    @Operation(summary = "Get command image", description = "Retrieves an image associated with a specific command using date components (year, month, day), command ID and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Image successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
    })
    public ResponseEntity<?> getCommandImage(
            @Parameter(description = "Year of the command", required = true) @PathVariable String y,
            @Parameter(description = "Month of the command", required = true) @PathVariable String m,
            @Parameter(description = "Day of the command", required = true) @PathVariable String d,
            @Parameter(description = "ID of the command", required = true) @PathVariable String commandId,
            @Parameter(description = "ID of the image to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("command/%s/%s/%s/%s/%s", y, m, d, commandId, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping("/anomalie/{y}/{m}/{d}/{anomalieId}/{imageId}")
    @Operation(summary = "Get anomaly image", description = "Retrieves an image associated with a specific anomaly using date components (year, month, day), anomaly ID and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Image successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found", content = @Content)
    })
    public ResponseEntity<?> getAnomalieImage(
            @Parameter(description = "Year of the anomaly", required = true) @PathVariable String y,
            @Parameter(description = "Month of the anomaly", required = true) @PathVariable String m,
            @Parameter(description = "Day of the anomaly", required = true) @PathVariable String d,
            @Parameter(description = "ID of the anomaly", required = true) @PathVariable String anomalieId,
            @Parameter(description = "ID of the image to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("anomalie/%s/%s/%s/%s/%s", y, m, d, anomalieId, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping("/account/{accountId}/{imageId}")
    @Operation(summary = "Get account logo", description = "Retrieves the logo associated with a specific account using account ID and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Logo successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Logo not found", content = @Content)
    })
    public ResponseEntity<?> getAccountLogo(
            @Parameter(description = "ID of the account", required = true) @PathVariable String accountId,
            @Parameter(description = "ID of the logo to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("account/%s/%s", accountId, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping("/profil/{profilId}/{imageId}")
    @Operation(summary = "Get profile picture", description = "Retrieves the profile picture associated with a specific user profile using profile ID and image ID", responses = {
            @ApiResponse(responseCode = "200", description = "Profile picture successfully retrieved", content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Profile picture not found", content = @Content)
    })
    public ResponseEntity<?> getProfilPicture(
            @Parameter(description = "ID of the profile", required = true) @PathVariable String profilId,
            @Parameter(description = "ID of the image to retrieve", required = true) @PathVariable String imageId)
            throws IOException {

        String path = String.format("profil/%s/%s", profilId, imageId);

        File file = pictureService.findImageFile(path);
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }
}
