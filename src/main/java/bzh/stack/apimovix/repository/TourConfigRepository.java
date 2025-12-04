package bzh.stack.apimovix.repository;

import bzh.stack.apimovix.model.TourConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour gérer les configurations de tournées
 */
@Repository
public interface TourConfigRepository extends JpaRepository<TourConfig, UUID> {

    /**
     * Récupère toutes les configurations de tournée pour un compte donné
     */
    List<TourConfig> findByAccountId(UUID accountId);

    /**
     * Récupère les configurations de tournée actives pour un jour donné avec leur compte
     * @param dayBit le bit correspondant au jour (0=Lundi, 1=Mardi, etc.)
     */
    @Query(value = "SELECT tc.* FROM tour_configs tc " +
                   "INNER JOIN account a ON tc.account_id = a.id " +
                   "WHERE (tc.recurrence & :dayBit) > 0", nativeQuery = true)
    List<TourConfig> findByActiveDay(@Param("dayBit") int dayBit);

    /**
     * Récupère les configurations par zone
     */
    @Query("SELECT tc FROM TourConfig tc WHERE tc.zone.id = :zoneId")
    List<TourConfig> findByZoneId(@Param("zoneId") UUID zoneId);

    /**
     * Récupère les configurations par profil
     */
    @Query("SELECT tc FROM TourConfig tc WHERE tc.profil.id = :profilId")
    List<TourConfig> findByProfilId(@Param("profilId") UUID profilId);

    /**
     * Vérifie si une configuration existe pour un compte et un nom de tournée
     */
    boolean existsByAccountIdAndTourName(UUID accountId, String tourName);
}
