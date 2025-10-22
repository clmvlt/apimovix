package bzh.stack.apimovix.repository.anomalie;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Picture.AnomaliePicture;

@Repository
public interface AnomaliePictureRepository extends JpaRepository<AnomaliePicture, UUID> {
    @Query("SELECT ap FROM AnomaliePicture ap WHERE ap.anomalie.account = :account AND ap.anomalie.id = :id")
    List<AnomaliePicture> findAnomaliePictureByIdAndAccount(@Param("account") Account account, @Param("id") UUID id);

    List<AnomaliePicture> findByCreatedAtBefore(LocalDateTime cutoffDate);

    long countByCreatedAtBefore(LocalDateTime cutoffDate);
} 