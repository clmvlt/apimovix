package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Tarif;

@Repository
public interface TarifRepository extends JpaRepository<Tarif, UUID> {
    
    @Query("SELECT t FROM Tarif t WHERE t.account.id = :accountId ORDER BY t.kmMax ASC")
    List<Tarif> findTarifs(@Param("accountId") UUID accountId);

    @Query("SELECT t FROM Tarif t WHERE t.account.id = :accountId and t.id = :id")
    Tarif findTarif(@Param("id") UUID id, @Param("accountId") UUID accountId);
} 