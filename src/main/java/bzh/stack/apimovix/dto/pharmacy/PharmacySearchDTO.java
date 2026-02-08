package bzh.stack.apimovix.dto.pharmacy;

import lombok.Data;

@Data
public class PharmacySearchDTO {
    /**
     * Recherche simple : un seul champ qui cherche dans tous les elements (nom, ville, code postal, cip, adresse)
     * Si ce champ est renseigne, les champs detailles sont ignores
     */
    private String query;

    // Champs detailles pour la recherche avancee
    private String postalCode;
    private String city;
    private String cip;
    private String address;
    private String name;
    private Boolean isLocationValid;
    private String zoneId;
    private Boolean hasOrdered;
    private Boolean hasPhotos;

    // Pagination
    private Integer page;
    private Integer size;
}
