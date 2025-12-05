package bzh.stack.apimovix.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.ProfileCreationToken;

public interface ProfileCreationTokenRepository extends JpaRepository<ProfileCreationToken, UUID> {

    /**
     * Trouve un token par sa valeur et qui n'est pas encore utilisé
     */
    @Query("SELECT tcp FROM ProfileCreationToken tcp WHERE tcp.token = :token AND tcp.isUsed = false")
    Optional<ProfileCreationToken> findByTokenAndNotUsed(@Param("token") String token);

    /**
     * Trouve tous les tokens pour un compte donné
     */
    @Query("SELECT tcp FROM ProfileCreationToken tcp WHERE tcp.account = :account ORDER BY tcp.createdAt DESC")
    List<ProfileCreationToken> findByAccount(@Param("account") Account account);

    /**
     * Trouve tous les tokens non utilisés pour un compte
     */
    @Query("SELECT tcp FROM ProfileCreationToken tcp WHERE tcp.account = :account AND tcp.isUsed = false ORDER BY tcp.createdAt DESC")
    List<ProfileCreationToken> findValidTokensByAccount(@Param("account") Account account);

    /**
     * Compte le nombre de tokens non utilisés pour un compte
     */
    @Query("SELECT COUNT(tcp) FROM ProfileCreationToken tcp WHERE tcp.account = :account AND tcp.isUsed = false")
    Long countValidTokensByAccount(@Param("account") Account account);

    /**
     * Vérifie si un token existe (par sa valeur)
     */
    boolean existsByToken(String token);

    /**
     * Invalide tous les tokens non utilisés pour un compte
     */
    @Modifying
    @Query("UPDATE ProfileCreationToken tcp SET tcp.isUsed = true, tcp.usedAt = CURRENT_TIMESTAMP WHERE tcp.account = :account AND tcp.isUsed = false")
    void invalidateAllTokensForAccount(@Param("account") Account account);
}
