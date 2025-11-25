package bzh.stack.apimovix.repository.update;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import bzh.stack.apimovix.model.MobileUpdate;

public interface MobileUpdateRepository extends JpaRepository<MobileUpdate, UUID> {

    @Query("SELECT mu FROM MobileUpdate mu ORDER BY mu.createdAt DESC LIMIT 1")
    Optional<MobileUpdate> findLatestVersion();

    @Query("SELECT mu FROM MobileUpdate mu ORDER BY mu.createdAt DESC")
    List<MobileUpdate> findAllOrderByCreatedAtDesc();

    Optional<MobileUpdate> findByVersion(String version);
} 