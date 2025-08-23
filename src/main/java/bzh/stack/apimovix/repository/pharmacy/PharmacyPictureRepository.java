package bzh.stack.apimovix.repository.pharmacy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Picture.PharmacyPicture;

@Repository
public interface PharmacyPictureRepository extends JpaRepository<PharmacyPicture, UUID> {
    @Query("SELECT p FROM PharmacyPicture p WHERE p.pharmacy.cip = :cip AND p.id = :id")
    PharmacyPicture findPicture(@Param("cip") String cip , @Param("id") UUID id );
} 