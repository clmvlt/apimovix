package bzh.stack.apimovix.dto.anomalie;

import java.time.LocalDateTime;
import java.util.UUID;

import bzh.stack.apimovix.dto.pharmacy.PharmacyDTO;
import bzh.stack.apimovix.dto.profil.ProfilDTO;
import bzh.stack.apimovix.model.StatusType.TypeAnomalie;
import lombok.Data;

@Data
public class AnomalieDTO {
    private UUID id;
    private String other;
    private String actions;
    private LocalDateTime createdAt;
    private PharmacyDTO pharmacy;
    private TypeAnomalie typeAnomalie;
    private ProfilDTO profil;
} 