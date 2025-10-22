package bzh.stack.apimovix.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/maintenance", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "API for system maintenance operations")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@ApiResponse(responseCode = "403", description = GLOBAL.ERROR_403, content = @Content)
public class MaintenanceController {

    private final PictureService pictureService;

    @GetMapping("/old-pictures-count")
    @HyperAdminRequired
    @Operation(
        summary = "Get count of old command pictures",
        description = "Returns the number of command pictures older than 3 months that will be deleted",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved count",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
        }
    )
    public ResponseEntity<?> getOldPicturesCount() {
        long count = pictureService.getOldCommandPicturesCount();

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("message", String.format("%d photos de commandes de plus de 3 mois seront supprimées", count));

        return MAPIR.ok(response);
    }

    @DeleteMapping("/old-pictures")
    @HyperAdminRequired
    @Operation(
        summary = "Delete old command pictures",
        description = "Deletes all command pictures older than 3 months",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted old command pictures",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
        }
    )
    public ResponseEntity<?> deleteOldPictures() {
        int deletedCount = pictureService.deleteOldCommandPictures();

        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", String.format("%d photos de commandes ont été supprimées", deletedCount));

        return MAPIR.ok(response);
    }
}