package bzh.stack.apimovix.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


public class MAPIR {
    /**
     * Retourne une réponse avec le code d'erreur HTTP 400 (Bad Request)
     * @param message Le message d'erreur à retourner
     * @return ResponseEntity avec le code 400
     */
    public static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(message);
    }

    /**
     * Retourne une réponse avec le code HTTP 200 (OK)
     * @param data Les données à retourner
     * @return ResponseEntity avec le code 200
     */
    public static ResponseEntity<?> ok(Object data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Retourne une réponse avec le code HTTP 200 (OK)
     * @param data Les données à retourner
     * @return ResponseEntity avec le code 200
     */
    public static ResponseEntity<?> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Retourne une réponse avec le code HTTP 200 (OK)
     * @param data Les données à retourner
     * @return ResponseEntity avec le code 200
     */
    public static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).build();
    }

    /**
     * Retourne une réponse avec le code HTTP 204 (No Content)
     * @return ResponseEntity avec le code 204
     */
    public static ResponseEntity<?> deleted() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Retourne une réponse avec le code HTTP 201 (Created)
     * @param data Les données créées à retourner
     * @return ResponseEntity avec le code 201
     */
    public static ResponseEntity<?> created(Object data) {
        return ResponseEntity.status(201).body(data);
    }

    /**
     * Retourne une réponse avec le code HTTP 400 (Bad Request)
     * @return ResponseEntity avec le code 400 et message indiquant que le body est requis
     */
    public static ResponseEntity<?> bodyIsMissing() {
        return badRequest("body " + GLOBAL.REQUIRED);
    }

    /**
     * Retourne une réponse avec le code HTTP 400 (Bad Request)
     * @param fieldName Le nom du champ requis
     * @return ResponseEntity avec le code 400 et message indiquant le champ requis
     */
    public static ResponseEntity<?> fieldRequired(String fieldName) {
        return badRequest(fieldName + " " + GLOBAL.REQUIRED);
    }

    /**
     * Retourne une réponse avec le code HTTP 400 (Bad Request)
     * @param fieldsNames Les noms des champs requis
     * @return ResponseEntity avec le code 400 et message indiquant les champs requis
     */
    public static ResponseEntity<?> fieldsRequired(String... fieldsNames) {
        return badRequest(String.join(GLOBAL.REQUIRED + "\n", fieldsNames));
    }

    /**
     * Retourne une réponse avec le code HTTP 404 (Not Found)
     * @return ResponseEntity avec le code 404
     */
    public static ResponseEntity<?> notFound() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Retourne une réponse avec le code HTTP 500 (Internal Server Error)
     * @return ResponseEntity avec le code 500
     */
    public static ResponseEntity<?> internalServerError() {
        return ResponseEntity.internalServerError().build();
    }

    /**
     * Retourne une réponse avec le code HTTP 200 (OK) contenant un fichier
     * @param file Le fichier à retourner
     * @return ResponseEntity avec le code 200 et le contenu du fichier
     */
    public static ResponseEntity<?> file(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());
            String fileName = file.getName();

            // Fallback pour les types MIME non reconnus
            if (contentType == null) {
                if (fileName.toLowerCase().endsWith(".apk")) {
                    contentType = "application/vnd.android.package-archive";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (IOException e) {
            return MAPIR.internalServerError();
        }
    }

    /**
     * Retourne une réponse avec le code HTTP 401 (Unauthorized)
     * @return ResponseEntity avec le code 401
     */
    public static ResponseEntity<?> invalidCredentials() {
        return ResponseEntity.status(401).body(GLOBAL.ERROR_401);
    }

    /**
     * Retourne une réponse avec le code HTTP 200 (OK) contenant un fichier PDF
     * @param pdfBytes Les octets du fichier PDF
     * @param filename Le nom du fichier PDF
     * @return ResponseEntity avec le code 200 et le contenu du PDF
     */
    public static ResponseEntity<?> pdf(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Retourne une réponse avec le code HTTP 409 (Conflict)
     * @return ResponseEntity avec le code 409
     */
    public static ResponseEntity<?> conflict(String message) {
        return ResponseEntity.status(409).body(message);
    }

    /**
     * Retourne une réponse avec le code HTTP 410 (Gone)
     * @param message Le message indiquant que la ressource n'est plus disponible
     * @return ResponseEntity avec le code 410
     */
    public static ResponseEntity<?> gone(String message) {
        return ResponseEntity.status(410).body(message);
    }
} 