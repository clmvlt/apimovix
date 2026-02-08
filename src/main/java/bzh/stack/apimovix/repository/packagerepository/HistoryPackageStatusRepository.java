package bzh.stack.apimovix.repository.packagerepository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;

@Repository
public interface HistoryPackageStatusRepository extends JpaRepository<HistoryPackageStatus, UUID> {
    void deleteByPackageEntity(PackageEntity packageEntity);

    @Query(value = "SELECT loading.tour_id, loading.first_loaded, livraison.livraison_at " +
            "FROM (" +
            "  SELECT c.id_tour as tour_id, MIN(hps.created_at) as first_loaded " +
            "  FROM history_package_status hps " +
            "  JOIN package p ON hps.barcode = p.barcode " +
            "  JOIN command c ON p.id_command = c.id " +
            "  WHERE hps.id_status = 2 AND c.id_tour IN (:tourIds) " +
            "  GROUP BY c.id_tour" +
            ") loading " +
            "LEFT JOIN (" +
            "  SELECT hts.id_tour as tour_id, MAX(hts.created_at) as livraison_at " +
            "  FROM history_tour_status hts " +
            "  WHERE hts.id_status = 3 AND hts.id_tour IN (:tourIds) " +
            "  GROUP BY hts.id_tour" +
            ") livraison ON loading.tour_id = livraison.tour_id", nativeQuery = true)
    List<Object[]> findLoadingTimesByTourIds(@Param("tourIds") List<String> tourIds);
}
