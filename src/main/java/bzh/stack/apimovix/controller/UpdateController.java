package bzh.stack.apimovix.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.model.MobileUpdate;
import bzh.stack.apimovix.service.update.FileService;
import bzh.stack.apimovix.service.update.MobileUpdateService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/updates", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Updates", description = "API for managing mobile application updates")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class UpdateController {

    private final MobileUpdateService mobileUpdateService;
    private final FileService fileService;

    @GetMapping("/latest")
    @Operation(summary = "Get latest version", description = "Retrieves the latest version of the mobile application", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved latest version", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MobileUpdate.class))),
            @ApiResponse(responseCode = "404", description = "No version found", content = @Content)
    })
    public ResponseEntity<?> getLatestVersion() {
        Optional<MobileUpdate> latestVersion = mobileUpdateService.getLatestVersion();
        if (latestVersion.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(latestVersion.get());
    }

    @GetMapping("/{version}")
    @Operation(summary = "Get specific version", description = "Retrieves a specific version of the mobile application", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved version", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MobileUpdate.class))),
            @ApiResponse(responseCode = "404", description = "Version not found", content = @Content)
    })
    public ResponseEntity<?> getVersion(
            @Parameter(description = "Version to retrieve", required = true) @PathVariable String version) {
        Optional<MobileUpdate> update = mobileUpdateService.getUpdateByVersion(version);
        if (update.isEmpty()) {
            return MAPIR.notFound();
        }
        return MAPIR.ok(update.get());
    }

    @PostMapping("/{version}")
    @HyperAdminRequired
    @Operation(summary = "Add new version", description = "Adds a new version of the mobile application. The request body should contain the raw APK file bytes.", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully added new version", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MobileUpdate.class))),
            @ApiResponse(responseCode = "500", description = "Error processing APK file", content = @Content)
    })
    public ResponseEntity<?> addVersion(
            @Parameter(description = "Raw APK file bytes", required = true) @RequestBody byte[] apkBytes,
            @Parameter(description = "Version number (e.g. '1.0.0')", required = true) @PathVariable String version) {
        Optional<MobileUpdate> update = mobileUpdateService.addUpdate(apkBytes, version);
        if (update.isEmpty()) {
            return MAPIR.internalServerError();
        }
        return MAPIR.created(update.get());
    }

    @DeleteMapping("/{version}")
    @HyperAdminRequired
    @Operation(summary = "Delete version", description = "Deletes a specific version of the mobile application", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted version", content = @Content),
            @ApiResponse(responseCode = "404", description = "Version not found", content = @Content)
    })
    public ResponseEntity<?> deleteVersion(
            @Parameter(description = "Version to delete", required = true) @PathVariable String version) {
        boolean deleted = mobileUpdateService.deleteUpdate(version);
        if (!deleted) {
            return MAPIR.notFound();
        }
        return MAPIR.noContent();
    }

    @GetMapping("/download/{version}")
    @Operation(summary = "Download APK", description = "Downloads the APK file for a specific version", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully downloaded APK", content = @Content(mediaType = "application/vnd.android.package-archive")),
            @ApiResponse(responseCode = "404", description = "Version not found", content = @Content)
    })
    public ResponseEntity<?> downloadApk(
            @Parameter(description = "Version to download", required = true) @PathVariable String version) throws IOException {
        Optional<MobileUpdate> update = mobileUpdateService.getUpdateByVersion(version);
        if (update.isEmpty()) {
            return MAPIR.notFound();
        }

        File file = fileService.findFile(update.get().getFilePath());
        if (file == null) {
            return MAPIR.notFound();
        }

        return MAPIR.file(file);
    }

    @GetMapping
    @Operation(summary = "Get all updates", description = "Retrieves all versions of the mobile application in order", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all versions", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MobileUpdate.class))),
            @ApiResponse(responseCode = "404", description = "No versions found", content = @Content)
    })
    public ResponseEntity<?> getAllUpdates() {
        return MAPIR.ok(mobileUpdateService.getAllUpdates());
    }
} 