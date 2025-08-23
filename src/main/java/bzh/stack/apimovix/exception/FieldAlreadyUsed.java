package bzh.stack.apimovix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import bzh.stack.apimovix.util.GLOBAL;

@ResponseStatus(HttpStatus.CONFLICT)
public class FieldAlreadyUsed extends RuntimeException {
    private final String field;

    public FieldAlreadyUsed(String field) {
        super(GLOBAL.ALREADY_USED);
        this.field = field;
    }

    public String getField() {
        return field;
    }
} 