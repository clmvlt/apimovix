package bzh.stack.apimovix.dto.command;

import lombok.Data;

@Data
public class CommandSearchDTO {
    private String pharmacyName;
    private String pharmacyCity;
    private String pharmacyCip ;
    private String pharmacyPostalCode;
    private String pharmacyAddress;
    private String commandId;
} 