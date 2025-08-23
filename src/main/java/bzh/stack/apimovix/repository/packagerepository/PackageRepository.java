package bzh.stack.apimovix.repository.packagerepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, String> {

    @Query("SELECT DISTINCT p FROM PackageEntity p LEFT JOIN FETCH p.command c LEFT JOIN FETCH c.pharmacy ph WHERE p.barcode = :barcode")
    PackageEntity findPackage(@Param("barcode") String barcode);

    @Query("SELECT hs FROM HistoryPackageStatus hs WHERE hs.packageEntity.command.sender.account = :account AND hs.packageEntity.barcode = :barcode ORDER BY hs.createdAt DESC")
    public List<HistoryPackageStatus> findPackageHistory(@Param("account") Account account, @Param("barcode") String barcode);

    @Query("SELECT p FROM PackageEntity p WHERE p.command.sender.account = :account AND p.barcode IN :barcodes")
    List<PackageEntity> findAllByIdIn(@Param("account") Account account, @Param("barcodes") List<String> barcodes);
} 