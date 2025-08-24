package bzh.stack.apimovix.repository.tour;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {
    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.commands c WHERE t.initialDate = :date AND t.account = :account ORDER BY t.name, c.tourOrder")
    List<Tour> findByDate(@Param("account") Account account, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT t FROM Tour t " +
           "LEFT JOIN FETCH t.profil " +
           "LEFT JOIN FETCH t.lastHistoryStatus " +
           "WHERE t.initialDate = :date AND t.account = :account " +
           "ORDER BY t.name")
    List<Tour> findToursOptimizedByDate(@Param("account") Account account, @Param("date") LocalDate date);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.commands c WHERE t.initialDate BETWEEN :startDate AND :endDate AND t.account = :account ORDER BY t.initialDate, t.name, c.tourOrder")
    List<Tour> findByDateRange(@Param("account") Account account, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Tour t  LEFT JOIN FETCH t.commands c WHERE t.profil = :profil AND t.account = :account AND t.lastHistoryStatus.status.id IN (1, 2, 3) ORDER BY t.name, t.initialDate, c.tourOrder")
    List<Tour> findByProfile(@Param("account") Account account, @Param("profil") Profil profil);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.commands c WHERE t.id = :id AND t.account = :account ORDER BY t.name, c.tourOrder")
    Tour findTour(@Param("account") Account account, @Param("id") String id);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.commands c WHERE t.account = :account AND t.id IN :tourIds ORDER BY t.name, c.tourOrder")
    List<Tour> findAllByIdIn(@Param("account") Account account, @Param("tourIds") List<String> tourIds);

    @Query("SELECT c FROM Command c " +
           "LEFT JOIN FETCH c.pharmacy " +
           "LEFT JOIN FETCH c.lastHistoryStatus " +
           "WHERE c.tour.id IN :tourIds AND c.tour.account = :account " +
           "ORDER BY c.tour.id, c.tourOrder")
    List<bzh.stack.apimovix.model.Command> findCommandsByTourIds(@Param("account") Account account, @Param("tourIds") List<String> tourIds);
}
