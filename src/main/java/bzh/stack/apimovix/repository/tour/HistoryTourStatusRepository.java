package bzh.stack.apimovix.repository.tour;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.History.HistoryTourStatus;

@Repository
public interface HistoryTourStatusRepository extends JpaRepository<HistoryTourStatus, UUID> {
    void deleteByTourId(String tourId);
    
    @Query("SELECT hts FROM HistoryTourStatus hts WHERE hts.tour.id = :tourId ORDER BY hts.createdAt DESC")
    List<HistoryTourStatus> findByTourIdOrderByCreatedAtDesc(@Param("tourId") String tourId);
} 