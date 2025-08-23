package bzh.stack.apimovix.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Nettoie automatiquement les tokens expirés toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure en millisecondes
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            passwordResetTokenRepository.deleteExpiredTokens();
            log.info("Nettoyage des tokens de réinitialisation expirés effectué");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens expirés", e);
        }
    }
} 