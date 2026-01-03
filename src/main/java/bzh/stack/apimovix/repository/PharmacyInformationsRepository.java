package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.PharmacyInformations;

@Repository
public interface PharmacyInformationsRepository extends JpaRepository<PharmacyInformations, String> {

    @Query("SELECT pi.pharmacy FROM PharmacyInformations pi WHERE pi.zone.id = :zoneId AND pi.account.id = :accountId")
    List<Pharmacy> findPharmaciesByZoneId(@Param("zoneId") UUID zoneId, @Param("accountId") UUID accountId);

    Optional<PharmacyInformations> findByCipAndAccountId(String cip, UUID accountId);
}
