package bzh.stack.apimovix.repository.pharmacy;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.PharmacyInfos;

@Repository
public interface PharmacyInfosRepository extends JpaRepository<PharmacyInfos, UUID> {

    @Query("SELECT DISTINCT pi FROM PharmacyInfos pi LEFT JOIN FETCH pi.pictures WHERE pi.account = :account ORDER BY pi.createdAt DESC")
    public List<PharmacyInfos> findPharmaciesInfos(@Param("account") Account account);

    @Query("SELECT DISTINCT pi FROM PharmacyInfos pi LEFT JOIN FETCH pi.pictures WHERE pi.id = :id AND pi.account = :account")
    public PharmacyInfos findPharmacyInfos(@Param("id") UUID id, @Param("account") Account account);
} 