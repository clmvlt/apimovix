package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandSearchDTO {
    private String pharmacyName;
    private String pharmacyCity;
    private String pharmacyCip ;
    private String pharmacyPostalCode;
    private String pharmacyAddress;
    private String commandId;
    
    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime startDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime endDate;

    private Integer max;
} 