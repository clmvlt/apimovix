package bzh.stack.apimovix.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.logs.LogContentDTO;
import bzh.stack.apimovix.dto.logs.LogFileDTO;
import bzh.stack.apimovix.service.LogService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@TokenRequired
@RequestMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Logs", description = "API for log files management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class LogsController {

    private final LogService logService;

    @GetMapping("/list")
    @Operation(
        summary = "List all log files",
        description = "Gets a list of all log files with their sizes and metadata (hyper admin required). Returns an array of log file objects with details like name, path, size, and last modified date.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved log files list",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = LogFileDTO.class))
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> listLogFiles() {
        try {
            List<LogFileDTO> logFiles = logService.getAllLogFiles();
            return MAPIR.ok(logFiles);
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la lecture des fichiers de logs: " + e.getMessage());
        }
    }

    @GetMapping("/content")
    @Operation(
        summary = "Get log file content",
        description = "Gets the content of a specific log file (hyper admin required). Use the relative path returned from the /list endpoint (e.g., '2025/10/22/no-token').",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved log file content",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LogContentDTO.class)
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> getLogFileContent(
            @Parameter(
                description = "Relative path of the log file from logs/api directory (e.g., '2025/10/22/no-token')",
                required = true,
                example = "2025/10/22/no-token"
            ) @RequestParam String path) {
        try {
            String content = logService.getLogFileContent(path);
            java.nio.file.Path logPath = java.nio.file.Paths.get("logs/api", path);
            long sizeBytes = java.nio.file.Files.size(logPath);

            LogContentDTO dto = new LogContentDTO(content, path, sizeBytes);
            return MAPIR.ok(dto);
        } catch (SecurityException e) {
            return MAPIR.badRequest("Accès non autorisé");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la lecture du fichier: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(
        summary = "Delete log file",
        description = "Deletes a specific log file (hyper admin required). Use the relative path returned from the /list endpoint.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted log file",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "string", example = "Fichier de log supprimé avec succès")
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> deleteLogFile(
            @Parameter(
                description = "Relative path of the log file to delete from logs/api directory (e.g., '2025/10/22/no-token')",
                required = true,
                example = "2025/10/22/no-token"
            ) @RequestParam String path) {
        try {
            logService.deleteLogFile(path);
            return MAPIR.ok("Fichier de log supprimé avec succès");
        } catch (SecurityException e) {
            return MAPIR.badRequest("Accès non autorisé");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-all")
    @Operation(
        summary = "Delete all log files",
        description = "Deletes all log files from the logs/api directory recursively (hyper admin required). ⚠️ This action cannot be undone!",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted all log files",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "string", example = "Tous les fichiers de log ont été supprimés avec succès")
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> deleteAllLogFiles() {
        try {
            logService.deleteAllLogFiles();
            return MAPIR.ok("Tous les fichiers de log ont été supprimés avec succès");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la suppression des fichiers: " + e.getMessage());
        }
    }
}
