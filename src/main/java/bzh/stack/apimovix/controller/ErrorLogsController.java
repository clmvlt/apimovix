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
import bzh.stack.apimovix.service.ErrorLogService;
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
@RequestMapping(value = "/errors/logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Error Logs", description = "API for error log files management")
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@RequiredArgsConstructor
public class ErrorLogsController {

    private final ErrorLogService errorLogService;

    @GetMapping("/list")
    @Operation(
        summary = "List all error log files",
        description = "Gets a list of all error log files with their sizes and metadata (hyper admin required). Returns an array of log file objects with details like name, path, size, and last modified date.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved error log files list",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = LogFileDTO.class))
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> listErrorLogFiles() {
        try {
            List<LogFileDTO> logFiles = errorLogService.getAllErrorLogFiles();
            return MAPIR.ok(logFiles);
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la lecture des fichiers d'erreurs: " + e.getMessage());
        }
    }

    @GetMapping("/content")
    @Operation(
        summary = "Get error log file content",
        description = "Gets the content of a specific error log file (hyper admin required). Use the relative path returned from the /list endpoint (e.g., '2025/10/22/no-token').",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved error log file content",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LogContentDTO.class)
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> getErrorLogFileContent(
            @Parameter(
                description = "Relative path of the error log file from logs/errors directory (e.g., '2025/10/22/no-token')",
                required = true,
                example = "2025/10/22/no-token"
            ) @RequestParam String path) {
        try {
            LogContentDTO dto = errorLogService.getErrorLogFileContent(path);
            return MAPIR.ok(dto);
        } catch (SecurityException e) {
            return MAPIR.badRequest("Acces non autorise");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la lecture du fichier: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(
        summary = "Delete error log file",
        description = "Deletes a specific error log file (hyper admin required). Use the relative path returned from the /list endpoint.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted error log file",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "string", example = "Fichier d'erreur supprime avec succes")
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> deleteErrorLogFile(
            @Parameter(
                description = "Relative path of the error log file to delete from logs/errors directory (e.g., '2025/10/22/no-token')",
                required = true,
                example = "2025/10/22/no-token"
            ) @RequestParam String path) {
        try {
            errorLogService.deleteErrorLogFile(path);
            return MAPIR.ok("Fichier d'erreur supprime avec succes");
        } catch (SecurityException e) {
            return MAPIR.badRequest("Acces non autorise");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-all")
    @Operation(
        summary = "Delete all error log files",
        description = "Deletes all error log files from the logs/errors directory recursively (hyper admin required). This action cannot be undone!",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully deleted all error log files",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "string", example = "Tous les fichiers d'erreur ont ete supprimes avec succes")
                )
            )
        }
    )
    @HyperAdminRequired
    public ResponseEntity<?> deleteAllErrorLogFiles() {
        try {
            errorLogService.deleteAllErrorLogFiles();
            return MAPIR.ok("Tous les fichiers d'erreur ont ete supprimes avec succes");
        } catch (IOException e) {
            return MAPIR.badRequest("Erreur lors de la suppression des fichiers: " + e.getMessage());
        }
    }
}
