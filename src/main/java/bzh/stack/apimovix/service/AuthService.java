package bzh.stack.apimovix.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import bzh.stack.apimovix.dto.auth.LoginRequestDTO;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ProfileRepository profilRepository;

    public Optional<Profil> login(LoginRequestDTO creds) {
        String identifiant = creds.getIdentifiant();
        Optional<Profil> profilOpt = profilRepository.findByEmail(identifiant);

        if (profilOpt.isEmpty()) {
            profilOpt = profilRepository.findByIdentifiant(identifiant);
        }
        
        if (profilOpt.isPresent()) {
            Profil profil = profilOpt.get();

            // Vérifier que le profil est actif
            if (Boolean.FALSE.equals(profil.getIsActive())) {
                log.warn("Tentative de connexion avec un profil inactif: {}", identifiant);
                return Optional.empty();
            }

            // Vérifier que l'account associé est actif
            if (profil.getAccount() != null && Boolean.FALSE.equals(profil.getAccount().getIsActive())) {
                log.warn("Tentative de connexion avec un compte desactive: {}", identifiant);
                return Optional.empty();
            }

            String hashedPassword = hashPassword(creds.getPassword());

            if (profil.getPasswordHash().equals(hashedPassword)) {
                return Optional.of(profil);
            }
        }

        return Optional.empty();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public Optional<Profil> findProfilByToken(String token) {
        return profilRepository.findByToken(token);
    }
} 