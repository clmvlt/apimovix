package bzh.stack.apimovix.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bzh.stack.apimovix.model.PasswordResetToken;
import bzh.stack.apimovix.model.Profil;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false")
    public Optional<PasswordResetToken> findByTokenAndNotUsed(@Param("token") String token);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.profil = :profil AND prt.used = false ORDER BY prt.createdAt DESC")
    public java.util.List<PasswordResetToken> findValidTokensByProfil(@Param("profil") Profil profil);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.profil = :profil AND prt.used = false")
    public void invalidateAllTokensForProfil(@Param("profil") Profil profil);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < CURRENT_TIMESTAMP")
    public void deleteExpiredTokens();
} 