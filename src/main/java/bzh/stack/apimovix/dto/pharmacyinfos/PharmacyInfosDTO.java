package bzh.stack.apimovix.dto.pharmacyinfos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;
import lombok.Data;

@Data
public class PharmacyInfosDTO {
    private UUID id;
    private String commentaire;
    private Boolean invalidGeocodage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PharmacyDTO pharmacy;
    private ProfilDTO profil;
    private List<PharmacyInfosPicture> pictures = new ArrayList<>();
}
