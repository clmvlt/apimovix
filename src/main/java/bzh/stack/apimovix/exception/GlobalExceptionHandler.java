package bzh.stack.apimovix.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        StringBuilder errorMessage = new StringBuilder();
        int count = 0;
        int total = errors.size();
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            errorMessage.append(entry.getKey()).append(" ").append(entry.getValue());
            if (count < total - 1) {
                errorMessage.append("\n");
            }
            count++;
        }
        
        return MAPIR.badRequest(errorMessage.toString());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        String message = ex.getAllValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(error -> error.getDefaultMessage())
            .findFirst()
            .orElse(GLOBAL.INVALID_FORMAT_PARAMETER);
            
        return MAPIR.badRequest(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatEx = (InvalidFormatException) ex.getCause();
            String fieldName = invalidFormatEx.getPath().get(0).getFieldName();
            return MAPIR.badRequest(fieldName + " " + GLOBAL.NUMBER);
        }

        // Gestion spécifique des erreurs de parsing JSON
        String message = ex.getMessage();
        if (message != null && message.contains("JSON parse error")) {
            if (message.contains("Unexpected character") && message.contains("was expecting double-quote")) {
                return MAPIR.badRequest("Format JSON invalide : caractère inattendu. Vérifiez que tous les noms de champs sont entre guillemets doubles.");
            } else if (message.contains("Unexpected end-of-input")) {
                return MAPIR.badRequest("Format JSON invalide : JSON incomplet ou malformé.");
            } else {
                return MAPIR.badRequest("Format JSON invalide : " + message);
            }
        }

        return MAPIR.badRequest(GLOBAL.INVALID_FORMAT_PARAMETER);
    }

    @ExceptionHandler(FieldAlreadyUsed.class)
    public ResponseEntity<?> handleFieldAlreadyUsed(FieldAlreadyUsed ex) {
        return MAPIR.conflict(ex.getField() + " " + GLOBAL.ALREADY_USED);
    }
} 