package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.PATTERNS;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CommandUpdateDTO {
    @JsonDeserialize(using = bzh.stack.apimovix.config.jackson.FlexibleLocalDateTimeDeserializer.class)
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime expDate;

    private String comment;

    private Boolean isForced;

    @NotEmpty(message = GLOBAL.CANNOT_BE_EMPTY)
    @NotNull(message = GLOBAL.REQUIRED)
    @Valid
    private List<@Pattern(regexp = PATTERNS.UUID_PATTERN, message = GLOBAL.INVALID_FORMAT_UUID) String> commandIds = new ArrayList<>();

    @JsonDeserialize(using = bzh.stack.apimovix.config.jackson.FlexibleLocalDateTimeDeserializer.class)
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime createdAt;
}
