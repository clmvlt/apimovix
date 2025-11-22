package bzh.stack.apimovix.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.ImporterToken;
import bzh.stack.apimovix.repository.ImporterTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImporterTokenService {

    private final ImporterTokenRepository importerTokenRepository;

    @Transactional(readOnly = true)
    public List<ImporterToken> findAll() {
        return importerTokenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ImporterToken> findById(UUID id) {
        return importerTokenRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ImporterToken> findByToken(String token) {
        return importerTokenRepository.findByToken(token);
    }

    @Transactional(readOnly = true)
    public Optional<ImporterToken> findActiveByToken(String token) {
        return importerTokenRepository.findByTokenAndIsActiveTrue(token);
    }

    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        return importerTokenRepository.findByTokenAndIsActiveTrue(token).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isBetaProxyToken(String token) {
        return importerTokenRepository.findByTokenAndIsActiveTrueAndIsBetaProxyTrue(token).isPresent();
    }

    @Transactional
    public ImporterToken create(String name, String description, Boolean isBetaProxy) {
        ImporterToken importerToken = new ImporterToken();
        importerToken.setName(name);
        importerToken.setDescription(description);
        importerToken.setToken(generateToken());
        importerToken.setIsActive(true);
        importerToken.setIsBetaProxy(isBetaProxy != null ? isBetaProxy : false);
        return importerTokenRepository.save(importerToken);
    }

    @Transactional
    public ImporterToken update(UUID id, String name, String description, Boolean isActive, Boolean isBetaProxy) {
        ImporterToken importerToken = importerTokenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Token non trouvé"));

        if (name != null) {
            importerToken.setName(name);
        }
        if (description != null) {
            importerToken.setDescription(description);
        }
        if (isActive != null) {
            importerToken.setIsActive(isActive);
        }
        if (isBetaProxy != null) {
            importerToken.setIsBetaProxy(isBetaProxy);
        }

        return importerTokenRepository.save(importerToken);
    }

    @Transactional
    public void delete(UUID id) {
        importerTokenRepository.deleteById(id);
    }

    @Transactional
    public void updateLastUsed(String token) {
        importerTokenRepository.findByToken(token).ifPresent(t -> {
            t.setLastUsedAt(LocalDateTime.now());
            importerTokenRepository.save(t);
        });
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
