package bzh.stack.apimovix.repository.anomalie;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;

@Repository
public interface AnomalieRepository extends JpaRepository<Anomalie, UUID> {

    @Query("SELECT a FROM Anomalie a WHERE a.account = :account ORDER BY a.createdAt DESC LIMIT 100")
    List<Anomalie> findAnomalies(@Param("account") Account account);

    @Query("SELECT DISTINCT a FROM Anomalie a LEFT JOIN FETCH a.packages WHERE a.account = :account AND a.id = :id")
    Anomalie findAnomalie(@Param("account") Account account, @Param("id") UUID id);
    
    @Query("SELECT DISTINCT a FROM Anomalie a LEFT JOIN FETCH a.packages " +
           "WHERE a.account = :account " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND (:cip IS NULL OR a.pharmacy.cip = :cip) " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "ORDER BY a.createdAt DESC")
    List<Anomalie> searchAnomalies(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("cip") String cip,
        @Param("typeCode") String typeCode
    );
    
    @Query("SELECT DISTINCT a FROM Anomalie a LEFT JOIN FETCH a.packages " +
           "WHERE a.account = :account " +
           "AND (:userId IS NULL OR a.profil.id = :userId) " +
           "AND a.createdAt >= :dateDebut " +
           "AND a.createdAt <= :dateFin " +
           "AND (:cip IS NULL OR a.pharmacy.cip = :cip) " +
           "AND (:typeCode IS NULL OR a.typeAnomalie.code = :typeCode) " +
           "ORDER BY a.createdAt DESC")
    List<Anomalie> searchAnomaliesWithDateRange(
        @Param("account") Account account,
        @Param("userId") UUID userId,
        @Param("dateDebut") LocalDateTime dateDebut,
        @Param("dateFin") LocalDateTime dateFin,
        @Param("cip") String cip,
        @Param("typeCode") String typeCode
    );
} 