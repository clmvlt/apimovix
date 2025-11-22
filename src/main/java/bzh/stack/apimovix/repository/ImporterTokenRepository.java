package bzh.stack.apimovix.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import bzh.stack.apimovix.model.ImporterToken;

public interface ImporterTokenRepository extends JpaRepository<ImporterToken, UUID> {
    Optional<ImporterToken> findByToken(String token);

    Optional<ImporterToken> findByTokenAndIsActiveTrue(String token);

    Optional<ImporterToken> findByTokenAndIsActiveTrueAndIsBetaProxyTrue(String token);

    boolean existsByToken(String token);
}
