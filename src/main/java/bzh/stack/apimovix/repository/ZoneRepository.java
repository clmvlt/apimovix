package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    @Query("SELECT DISTINCT z FROM Zone z LEFT JOIN FETCH z.pharmacies WHERE z.id = :id AND z.account = :account")
    public Optional<Zone> findZone(@Param("account") Account account, @Param("id") UUID id);
    @Query("SELECT DISTINCT z FROM Zone z LEFT JOIN FETCH z.pharmacies WHERE z.id = :id")
    public Optional<Zone> findZone(@Param("id") UUID id);

    @Query("SELECT DISTINCT z FROM Zone z WHERE z.account = :account ORDER BY z.name")
    public List<Zone> findZones(@Param("account") Account account);
}

