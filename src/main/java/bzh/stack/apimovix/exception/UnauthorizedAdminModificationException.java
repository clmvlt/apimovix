package bzh.stack.apimovix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAdminModificationException extends RuntimeException {
    public UnauthorizedAdminModificationException() {
        super("Vous devez avoir les droits administrateur pour modifier les permissions admin");
    }
}
