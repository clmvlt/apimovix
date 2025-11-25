package bzh.stack.apimovix.repository.pharmacy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Picture.PharmacyPicture;

@Repository
public interface PharmacyPictureRepository extends JpaRepository<PharmacyPicture, UUID> {
    @Query("SELECT p FROM PharmacyPicture p WHERE p.pharmacy.cip = :cip AND p.id = :id AND p.account.id = :accountId")
    PharmacyPicture findPicture(@Param("cip") String cip, @Param("id") UUID id, @Param("accountId") UUID accountId);

    @Query("SELECT p FROM PharmacyPicture p WHERE p.pharmacy.cip = :cip AND p.account.id = :accountId")
    List<PharmacyPicture> findPicturesByPharmacyAndAccount(@Param("cip") String cip, @Param("accountId") UUID accountId);

    List<PharmacyPicture> findByCreatedAtBefore(LocalDateTime cutoffDate);

    long countByCreatedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT COALESCE(MAX(p.displayOrder), 0) FROM PharmacyPicture p WHERE p.pharmacy.cip = :cip AND p.account.id = :accountId")
    Integer findMaxDisplayOrderByPharmacyAndAccount(@Param("cip") String cip, @Param("accountId") UUID accountId);
} 