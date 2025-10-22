package bzh.stack.apimovix.repository.packagerepository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;

@Repository
public interface HistoryPackageStatusRepository extends JpaRepository<HistoryPackageStatus, UUID> {
    void deleteByPackageEntity(PackageEntity packageEntity);
} 