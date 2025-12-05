package bzh.stack.apimovix.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.profil.ProfilCreateDTO;
import bzh.stack.apimovix.dto.token.ProfilCreateWithTokenDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.ProfileCreationToken;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.ProfileRepository;
import bzh.stack.apimovix.repository.ProfileCreationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCreationTokenService {

    private final ProfileCreationTokenRepository tokenCreationProfilRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Génère un token unique et sécurisé
     */
    private String generateUniqueToken() {
        String token;
        do {
            byte[] randomBytes = new byte[48]; // 48 bytes = 64 caractères en base64
            secureRandom.nextBytes(randomBytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        } while (tokenCreationProfilRepository.existsByToken(token));
        return token;
    }

    /**
     * Crée un nouveau token de création de profil pour un compte
     *
     * @param accountId ID du compte
     * @param notes Notes optionnelles
     * @return Le token créé
     */
    @Transactional
    public ProfileCreationToken createToken(UUID accountId, String notes) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));

        ProfileCreationToken token = new ProfileCreationToken();
        token.setToken(generateUniqueToken());
        token.setAccount(account);
        token.setNotes(notes);
        token.setIsUsed(false);

        ProfileCreationToken savedToken = tokenCreationProfilRepository.save(token);
        log.info("Token de création de profil créé pour le compte {}", accountId);

        return savedToken;
    }

    /**
     * Valide et récupère un token non utilisé
     *
     * @param tokenValue La valeur du token
     * @return Le token s'il est valide
     */
    public Optional<ProfileCreationToken> validateToken(String tokenValue) {
        return tokenCreationProfilRepository.findByTokenAndNotUsed(tokenValue);
    }

    /**
     * Marque un token comme utilisé
     *
     * @param tokenValue La valeur du token
     * @return Le token marqué comme utilisé
     */
    @Transactional
    public ProfileCreationToken useToken(String tokenValue) {
        ProfileCreationToken token = tokenCreationProfilRepository.findByTokenAndNotUsed(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou déjà utilisé"));

        token.markAsUsed();
        ProfileCreationToken savedToken = tokenCreationProfilRepository.save(token);

        log.info("Token de création de profil utilisé pour le compte {}", token.getAccount().getId());

        return savedToken;
    }

    /**
     * Récupère tous les tokens pour un compte
     *
     * @param accountId ID du compte
     * @return Liste des tokens
     */
    public List<ProfileCreationToken> getTokensByAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        return tokenCreationProfilRepository.findByAccount(account);
    }

    /**
     * Récupère tous les tokens valides (non utilisés) pour un compte
     *
     * @param accountId ID du compte
     * @return Liste des tokens valides
     */
    public List<ProfileCreationToken> getValidTokensByAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        return tokenCreationProfilRepository.findValidTokensByAccount(account);
    }

    /**
     * Compte le nombre de tokens valides pour un compte
     *
     * @param accountId ID du compte
     * @return Nombre de tokens valides
     */
    public Long countValidTokensByAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        return tokenCreationProfilRepository.countValidTokensByAccount(account);
    }

    /**
     * Invalide tous les tokens non utilisés pour un compte
     *
     * @param accountId ID du compte
     */
    @Transactional
    public void invalidateAllTokensForAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        tokenCreationProfilRepository.invalidateAllTokensForAccount(account);
        log.info("Tous les tokens de création de profil invalidés pour le compte {}", accountId);
    }

    /**
     * Supprime un token
     *
     * @param tokenId ID du token
     */
    @Transactional
    public void deleteToken(UUID tokenId) {
        tokenCreationProfilRepository.deleteById(tokenId);
        log.info("Token de création de profil {} supprimé", tokenId);
    }

    /**
     * Récupère tous les tokens
     *
     * @return Liste de tous les tokens
     */
    public List<ProfileCreationToken> getAllTokens() {
        return tokenCreationProfilRepository.findAll();
    }

    /**
     * Crée un profil avec un token de création
     *
     * @param createDTO Données de création du profil avec token
     * @return Le profil créé
     */
    @Transactional
    public Profil createProfilWithToken(ProfilCreateWithTokenDTO createDTO) {
        // Valider et récupérer le token
        ProfileCreationToken token = tokenCreationProfilRepository.findByTokenAndNotUsed(createDTO.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou déjà utilisé"));

        Account account = token.getAccount();

        // Vérifier la limite de profils
        long currentProfileCount = profileRepository.countByAccount(account);
        Integer maxProfiles = account.getMaxProfiles();

        if (maxProfiles != null && maxProfiles > 0 && currentProfileCount >= maxProfiles) {
            throw new IllegalArgumentException("Limite de profils atteinte pour ce compte (" + maxProfiles + " maximum)");
        }

        // Vérifier si l'email existe déjà
        if (profileRepository.existsByEmail(createDTO.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Créer le DTO pour ProfileService
        ProfilCreateDTO profilCreateDTO = new ProfilCreateDTO();
        profilCreateDTO.setEmail(createDTO.getEmail());
        profilCreateDTO.setPassword(createDTO.getPassword());
        profilCreateDTO.setFirstName(createDTO.getFirstName());
        profilCreateDTO.setLastName(createDTO.getLastName());
        profilCreateDTO.setIsAdmin(createDTO.getIsAdmin() != null ? createDTO.getIsAdmin() : false);
        profilCreateDTO.setIsWeb(true);  // Par défaut accès web
        profilCreateDTO.setIsMobile(false);
        profilCreateDTO.setIsStock(false);
        profilCreateDTO.setIsAvtrans(false);
        profilCreateDTO.setIsActive(true);

        // Générer un identifiant unique basé sur l'email
        String baseIdentifiant = createDTO.getEmail().split("@")[0];
        String identifiant = baseIdentifiant;
        int counter = 1;
        while (profileRepository.existsByIdentifiant(identifiant)) {
            identifiant = baseIdentifiant + counter;
            counter++;
        }
        profilCreateDTO.setIdentifiant(identifiant);

        // Créer un profil "fantôme" admin pour pouvoir créer le vrai profil
        // (le service ProfileService nécessite un profil currentProfil avec isAdmin=true)
        Profil tempAdminProfil = new Profil();
        tempAdminProfil.setIsAdmin(true);

        // Créer le profil
        Profil profil = profileService.createProfile(account, profilCreateDTO, tempAdminProfil);

        // Marquer le token comme utilisé
        token.markAsUsed();
        tokenCreationProfilRepository.save(token);

        log.info("Profil {} créé avec le token pour le compte {}", profil.getId(), account.getId());

        return profil;
    }
}
