package bzh.stack.apimovix.repository.packagerepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.StatusType.PackageStatus;

@Repository
public interface PackageStatusRepository extends JpaRepository<PackageStatus, Integer> {
} 