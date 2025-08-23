package bzh.stack.apimovix.repository.tour;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.StatusType.TourStatus;

@Repository
public interface TourStatusRepository extends JpaRepository<TourStatus, Integer> {
    
}

