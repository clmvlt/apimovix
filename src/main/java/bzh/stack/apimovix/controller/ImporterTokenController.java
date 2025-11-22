package bzh.stack.apimovix.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.model.ImporterToken;
import bzh.stack.apimovix.service.ImporterTokenService;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/importer-tokens")
@RequiredArgsConstructor
@HyperAdminRequired
@Tag(name = "Importer Tokens", description = "Gestion des tokens d'importation (HyperAdmin)")
public class ImporterTokenController {

    private final ImporterTokenService importerTokenService;

    @GetMapping
    @Operation(summary = "Liste tous les tokens d'importation")
    public ResponseEntity<?> findAll() {
        List<ImporterToken> tokens = importerTokenService.findAll();
        return MAPIR.ok(tokens);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un token par son ID")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return importerTokenService.findById(id)
            .<ResponseEntity<?>>map(MAPIR::ok)
            .orElseGet(MAPIR::notFound);
    }

    @PostMapping
    @Operation(summary = "Crée un nouveau token d'importation")
    public ResponseEntity<?> create(@RequestBody CreateImporterTokenRequest request) {
        if (request.name == null || request.name.isBlank()) {
            return MAPIR.badRequest("Le nom est requis");
        }
        ImporterToken token = importerTokenService.create(request.name, request.description, request.isBetaProxy);
        return MAPIR.created(token);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un token d'importation")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateImporterTokenRequest request) {
        try {
            ImporterToken token = importerTokenService.update(id, request.name, request.description, request.isActive, request.isBetaProxy);
            return MAPIR.ok(token);
        } catch (RuntimeException e) {
            return MAPIR.notFound();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un token d'importation")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        if (importerTokenService.findById(id).isEmpty()) {
            return MAPIR.notFound();
        }
        importerTokenService.delete(id);
        return MAPIR.ok("Token supprimé");
    }

    public static class CreateImporterTokenRequest {
        public String name;
        public String description;
        public Boolean isBetaProxy;
    }

    public static class UpdateImporterTokenRequest {
        public String name;
        public String description;
        public Boolean isActive;
        public Boolean isBetaProxy;
    }
}
