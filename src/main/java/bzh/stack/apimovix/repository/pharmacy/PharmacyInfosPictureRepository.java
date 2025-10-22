package bzh.stack.apimovix.repository.pharmacy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Picture.PharmacyInfosPicture;

@Repository
public interface PharmacyInfosPictureRepository extends JpaRepository<PharmacyInfosPicture, UUID> {

    List<PharmacyInfosPicture> findByCreatedAtBefore(LocalDateTime cutoffDate);

    long countByCreatedAtBefore(LocalDateTime cutoffDate);
} 