package bzh.stack.apimovix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProfileLimitExceededException extends RuntimeException {
    public ProfileLimitExceededException(int currentCount, int maxAllowed) {
        super(String.format("Limite de profils atteinte: %d/%d profils créés", currentCount, maxAllowed));
    }
}
