package bzh.stack.apimovix.dto.command;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import bzh.stack.apimovix.util.PATTERNS;
import lombok.Data;

@Data
public class CommandSearchDTO {
    /**
     * Recherche simple : un seul champ qui cherche dans tous les elements
     * (nom pharmacie, ville, code postal, cip, adresse, id commande)
     * Si ce champ est renseigne, les champs detailles sont ignores
     */
    private String query;

    // Champs detailles pour la recherche avancee
    private String pharmacyName;
    private String pharmacyCity;
    private String pharmacyCip;
    private String pharmacyPostalCode;
    private String pharmacyAddress;
    private String commandId;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime startDate;

    @JsonFormat(pattern = PATTERNS.DATETIME)
    private LocalDateTime endDate;

    // Pagination
    private Integer page;
    private Integer size;
} 