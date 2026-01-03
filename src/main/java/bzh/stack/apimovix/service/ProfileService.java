package bzh.stack.apimovix.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.profil.ProfilCreateDTO;
import bzh.stack.apimovix.dto.profil.ProfilMobileUpdateDTO;
import bzh.stack.apimovix.dto.profil.ProfilUpdateDTO;
import bzh.stack.apimovix.exception.FieldAlreadyUsed;
import bzh.stack.apimovix.exception.ProfileLimitExceededException;
import bzh.stack.apimovix.exception.UnauthorizedAdminModificationException;
import bzh.stack.apimovix.mapper.ProfileMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.PasswordResetToken;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.PasswordResetTokenRepository;
import bzh.stack.apimovix.repository.ProfileRepository;
import bzh.stack.apimovix.service.picture.PictureService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final AccountRepository accountRepository;
    private final PictureService pictureService;

    @Value("${app.site-url}")
    private String siteUrl;

    @Transactional(readOnly = true)
    public Optional<Profil> findProfile(Account account, UUID id) {
        return Optional.ofNullable(profileRepository.findProfile(account, id));
    }

    @Transactional(readOnly = true)
    public List<Profil> findProfiles(Account account) {
        return profileRepository.findProfiles(account);
    }

    @Transactional
    public Profil createProfile(Account account, ProfilCreateDTO createDTO, Profil currentProfil) {
        // Vérifier la limite de profils (compte tous les profils actifs et inactifs)
        long currentProfileCount = profileRepository.countByAccount(account);
        Integer maxProfiles = account.getMaxProfiles();

        if (maxProfiles != null && maxProfiles > 0 && currentProfileCount >= maxProfiles) {
            throw new ProfileLimitExceededException((int) currentProfileCount, maxProfiles);
        }

        // Vérifier les permissions pour définir isAdmin
        if (Boolean.TRUE.equals(createDTO.getIsAdmin()) && !Boolean.TRUE.equals(currentProfil.getIsAdmin())) {
            throw new UnauthorizedAdminModificationException();
        }

        if (Boolean.TRUE.equals(createDTO.getIsWeb()) && profileRepository.existsByEmail(createDTO.getEmail())) {
            throw new FieldAlreadyUsed("email");
        }
        if (profileRepository.existsByIdentifiant(createDTO.getIdentifiant())) {
            throw new FieldAlreadyUsed("identifiant");
        }

        Profil profil = new Profil();
        profileMapper.updateEntityFromCreateDto(createDTO, profil);
        profil.setPasswordHash(hashPassword(createDTO.getPassword()));
        profil.setAccount(account);
        profil.setId(UUID.randomUUID());
        profil.setToken(generateSecureToken());

        // Si isActive n'est pas spécifié, mettre à true par défaut
        if (profil.getIsActive() == null) {
            profil.setIsActive(true);
        }

        // Handle profile picture if provided (base64)
        if (createDTO.getProfilPicture() != null && !createDTO.getProfilPicture().trim().isEmpty()) {
            String picturePath = pictureService.saveProfilPicture(profil, createDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return profileRepository.save(profil);
    }

    @Transactional
    public Optional<Profil> updateProfil(Account account, ProfilCreateDTO createDTO, UUID profilId, Profil currentProfil) {
        Optional<Profil> optProfil = findProfile(account, profilId);
        if (optProfil.isEmpty()) {
            return Optional.empty();
        }
        Profil profil = optProfil.get();

        // Vérifier les permissions pour modifier isAdmin
        if (createDTO.getIsAdmin() != null && !createDTO.getIsAdmin().equals(profil.getIsAdmin())) {
            if (!Boolean.TRUE.equals(currentProfil.getIsAdmin())) {
                throw new UnauthorizedAdminModificationException();
            }
        }

        // Valider l'email seulement si isWeb est true et que l'email est fourni
        if (Boolean.TRUE.equals(createDTO.getIsWeb()) && createDTO.getEmail() != null &&
            profileRepository.existsByEmailAndIdNot(createDTO.getEmail(), profilId)) {
            throw new FieldAlreadyUsed("email");
        }

        // Valider l'identifiant seulement s'il est fourni
        if (createDTO.getIdentifiant() != null && !createDTO.getIdentifiant().trim().isEmpty() &&
            profileRepository.existsByIdentifiantAndIdNot(createDTO.getIdentifiant(), profilId)) {
            throw new FieldAlreadyUsed("identifiant");
        }

        profileMapper.updateEntityFromCreateDto(createDTO, profil);

        // Ne mettre à jour le mot de passe que s'il est fourni
        if (createDTO.getPassword() != null && !createDTO.getPassword().trim().isEmpty()) {
            profil.setPasswordHash(hashPassword(createDTO.getPassword()));
        }

        // Handle profile picture if provided (base64)
        if (createDTO.getProfilPicture() != null && !createDTO.getProfilPicture().trim().isEmpty()) {
            // Delete old picture if exists
            if (profil.getProfilPicture() != null && !profil.getProfilPicture().trim().isEmpty()) {
                pictureService.deleteImage(profil.getProfilPicture());
            }
            String picturePath = pictureService.saveProfilPicture(profil, createDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return Optional.of(profileRepository.save(profil));
    }

    @Transactional
    public Optional<Profil> updateProfilWithoutPassword(Account account, ProfilUpdateDTO updateDTO, UUID profilId, Profil currentProfil) {
        Optional<Profil> optProfil = findProfile(account, profilId);
        if (optProfil.isEmpty()) {
            return Optional.empty();
        }
        Profil profil = optProfil.get();

        // Vérifier les permissions pour modifier isAdmin
        if (updateDTO.getIsAdmin() != null && !updateDTO.getIsAdmin().equals(profil.getIsAdmin())) {
            if (!Boolean.TRUE.equals(currentProfil.getIsAdmin())) {
                throw new UnauthorizedAdminModificationException();
            }
        }

        // Valider l'email seulement si isWeb est true et que l'email est fourni
        if (Boolean.TRUE.equals(updateDTO.getIsWeb()) && updateDTO.getEmail() != null &&
            profileRepository.existsByEmailAndIdNot(updateDTO.getEmail(), profilId)) {
            throw new FieldAlreadyUsed("email");
        }

        // Valider l'identifiant seulement s'il est fourni et différent de l'actuel
        if (updateDTO.getIdentifiant() != null && !updateDTO.getIdentifiant().trim().isEmpty() &&
            !updateDTO.getIdentifiant().equals(profil.getIdentifiant()) &&
            profileRepository.existsByIdentifiantAndIdNot(updateDTO.getIdentifiant(), profilId)) {
            throw new FieldAlreadyUsed("identifiant");
        }
        profileMapper.updateEntityFromUpdateDto(updateDTO, profil);

        // Ne mettre à jour le mot de passe que s'il est fourni et non vide
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            profil.setPasswordHash(hashPassword(updateDTO.getPassword()));
        }

        // Handle profile picture if provided (base64)
        if (updateDTO.getProfilPicture() != null && !updateDTO.getProfilPicture().trim().isEmpty()) {
            // Delete old picture if exists
            if (profil.getProfilPicture() != null && !profil.getProfilPicture().trim().isEmpty()) {
                pictureService.deleteImage(profil.getProfilPicture());
            }
            String picturePath = pictureService.saveProfilPicture(profil, updateDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return Optional.of(profileRepository.save(profil));
    }

    @Transactional
    public boolean delete(Account account, UUID uuid) {
        Profil profil = profileRepository.findProfile(account, uuid);
        if (profil == null) {
            return false;
        }
        profil.setDeleted(true);
        profileRepository.save(profil);
        return true;
    }

    @Transactional
    public boolean changePassword(Profil profil, String currentPassword, String newPassword) {
        // Vérifier que l'ancien mot de passe est correct
        String hashedCurrentPassword = hashPassword(currentPassword);
        if (!profil.getPasswordHash().equals(hashedCurrentPassword)) {
            return false;
        }

        // Hacher et sauvegarder le nouveau mot de passe
        String hashedNewPassword = hashPassword(newPassword);
        profil.setPasswordHash(hashedNewPassword);
        profileRepository.save(profil);
        
        return true;
    }

    /**
     * Génère un token de réinitialisation de mot de passe et l'envoie par email
     */
    @Transactional
    public boolean initiatePasswordReset(String email) {
        Optional<Profil> optProfil = profileRepository.findByEmail(email);
        if (optProfil.isEmpty()) {
            return false; // On retourne true même si l'email n'existe pas pour des raisons de sécurité
        }

        Profil profil = optProfil.get();
        
        // Invalider tous les tokens existants pour ce profil
        passwordResetTokenRepository.invalidateAllTokensForProfil(profil);
        
        // Générer un nouveau token sécurisé
        String token = generateSecureToken();
        
        // Créer le token de réinitialisation
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(UUID.randomUUID());
        resetToken.setToken(token);
        resetToken.setProfil(profil);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // Expire dans 24h
        resetToken.setUsed(false);
        
        passwordResetTokenRepository.save(resetToken);
        
        // Envoyer l'email
        sendPasswordResetEmail(profil, token);
        
        return true;
    }

    /**
     * Réinitialise le mot de passe avec un token valide
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> optResetToken = passwordResetTokenRepository.findByTokenAndNotUsed(token);
        if (optResetToken.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = optResetToken.get();
        
        // Vérifier que le token n'est pas expiré
        if (resetToken.isExpired()) {
            return false;
        }

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Changer le mot de passe
        Profil profil = resetToken.getProfil();
        String hashedNewPassword = hashPassword(newPassword);
        profil.setPasswordHash(hashedNewPassword);
        profileRepository.save(profil);
        
        return true;
    }

    /**
     * Génère un token sécurisé de 64 caractères
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 32 bytes = 256 bits
        random.nextBytes(bytes);
        
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        
        return token.toString();
    }

    /**
     * Envoie l'email de réinitialisation de mot de passe
     */
    private void sendPasswordResetEmail(Profil profil, String token) {
        String subject = "Réinitialisation de votre mot de passe - Movix";
        String resetUrl = siteUrl + "/reset-password?token=" + token;
        
        String htmlContent = generatePasswordResetEmailHtml(profil.getFullName(), resetUrl, token);
        
        try {
            emailService.sendHtmlEmail(profil.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la transaction
            System.err.println("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
        }
    }

    /**
     * Génère le contenu HTML de l'email de réinitialisation
     */
    private String generatePasswordResetEmailHtml(String fullName, String resetUrl, String token) {
        String name = fullName != null ? fullName : "Utilisateur";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de mot de passe</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <!-- Header -->
                    <div style="background-color: #F8F9FA; padding: 20px; text-align: center; border-bottom: 2px solid #123456;">
                        <h1 style="color: #2C3E50; margin: 0; font-size: 24px; font-weight: 600;">Movix</h1>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 25px 20px;">
                        <h2 style="color: #2C3E50; margin: 0 0 15px; font-size: 20px; font-weight: 600;">
                            Réinitialisation de votre mot de passe
                        </h2>
                        
                        <p style="color: #2C3E50; font-size: 14px; line-height: 1.5; margin-bottom: 20px;">
                            Bonjour %s,
                        </p>
                        
                        <p style="color: #2C3E50; font-size: 14px; line-height: 1.5; margin-bottom: 20px;">
                            Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte Movix.
                        </p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #123456; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: 600; display: inline-block;">
                                Réinitialiser mon mot de passe
                            </a>
                        </div>
                        
                        <p style="color: #2C3E50; font-size: 14px; line-height: 1.5; margin-bottom: 20px;">
                            Si le bouton ne fonctionne pas, vous pouvez copier et coller ce lien dans votre navigateur :
                        </p>
                        
                        <div style="background-color: #f8f9fa; padding: 10px; border-radius: 5px; margin: 20px 0;">
                            <p style="color: #2C3E50; font-size: 12px; margin: 0; word-break: break-all;">
                                <a href="%s" style="color: #123456;">%s</a>
                            </p>
                        </div>
                        
                        <p style="color: #2C3E50; font-size: 14px; line-height: 1.5; margin-bottom: 20px;">
                            Ce lien expire dans 24 heures. Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.
                        </p>
                        
                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p style="color: #856404; font-size: 13px; margin: 0;">
                                <strong>⚠️ Sécurité :</strong> Ne partagez jamais ce lien avec quelqu'un d'autre. Notre équipe ne vous demandera jamais vos informations de connexion par email.
                            </p>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #F8F9FA; padding: 15px; border-top: 1px solid #E9ECEF;">
                        <div style="max-width: 600px; margin: 0 auto;">
                            <p style="color: #856404; margin: 0 0 10px; font-size: 12px; background-color: #FFF3CD; padding: 8px; border-radius: 3px;">
                                ⚠️ Email automatique - Ne pas répondre
                            </p>
                            <p style="color: #2C3E50; margin: 0 0 8px; font-size: 12px; text-align: center;">
                                Support : <a href="mailto:contact@stack.bzh" style="color: #123456;">contact@stack.bzh</a>
                            </p>
                            <p style="color: #6C757D; margin: 0; font-size: 11px; text-align: center;">
                                © 2025 Movix
                            </p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, resetUrl, resetUrl, resetUrl);
    }

    /**
     * Check if an identifiant is already used
     */
    @Transactional(readOnly = true)
    public boolean isIdentifiantUsed(String identifiant) {
        return profileRepository.existsByIdentifiant(identifiant);
    }

    /**
     * Check if an email is already used
     */
    @Transactional(readOnly = true)
    public boolean isEmailUsed(String email) {
        return profileRepository.existsByEmail(email);
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

    // ============================================
    // HyperAdmin-only methods
    // ============================================

    /**
     * Find all profiles for a specific account (HyperAdmin only)
     */
    @Transactional(readOnly = true)
    public Optional<List<Profil>> findProfilesByAccountId(UUID accountId) {
        Optional<Account> optAccount = accountRepository.findById(accountId);
        if (optAccount.isEmpty()) {
            return Optional.empty();
        }
        List<Profil> profiles = profileRepository.findProfiles(optAccount.get());
        return Optional.of(profiles);
    }

    /**
     * Create profile for a specific account (HyperAdmin only)
     */
    @Transactional
    public Optional<Profil> createProfileForAccountByHyperAdmin(UUID accountId, ProfilCreateDTO createDTO) {
        Optional<Account> optAccount = accountRepository.findById(accountId);
        if (optAccount.isEmpty()) {
            return Optional.empty();
        }
        Account account = optAccount.get();

        // Vérifier la limite de profils
        long currentProfileCount = profileRepository.countByAccount(account);
        Integer maxProfiles = account.getMaxProfiles();

        if (maxProfiles != null && maxProfiles > 0 && currentProfileCount >= maxProfiles) {
            throw new ProfileLimitExceededException((int) currentProfileCount, maxProfiles);
        }

        // Vérifier les champs uniques
        if (Boolean.TRUE.equals(createDTO.getIsWeb()) && profileRepository.existsByEmail(createDTO.getEmail())) {
            throw new FieldAlreadyUsed("email");
        }
        if (profileRepository.existsByIdentifiant(createDTO.getIdentifiant())) {
            throw new FieldAlreadyUsed("identifiant");
        }

        Profil profil = new Profil();
        profileMapper.updateEntityFromCreateDto(createDTO, profil);
        profil.setPasswordHash(hashPassword(createDTO.getPassword()));
        profil.setAccount(account);
        profil.setId(UUID.randomUUID());
        profil.setToken(generateSecureToken());

        // Si isActive n'est pas spécifié, mettre à true par défaut
        if (profil.getIsActive() == null) {
            profil.setIsActive(true);
        }

        // Handle profile picture if provided (base64)
        if (createDTO.getProfilPicture() != null && !createDTO.getProfilPicture().trim().isEmpty()) {
            String picturePath = pictureService.saveProfilPicture(profil, createDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return Optional.of(profileRepository.save(profil));
    }

    /**
     * Update any profile (HyperAdmin only)
     */
    @Transactional
    public Optional<Profil> updateProfilByHyperAdmin(ProfilUpdateDTO updateDTO, UUID profilId) {
        Optional<Profil> optProfil = profileRepository.findById(profilId);
        if (optProfil.isEmpty()) {
            return Optional.empty();
        }
        Profil profil = optProfil.get();

        // Valider l'email seulement si isWeb est true et que l'email est fourni
        if (Boolean.TRUE.equals(updateDTO.getIsWeb()) && updateDTO.getEmail() != null &&
            profileRepository.existsByEmailAndIdNot(updateDTO.getEmail(), profilId)) {
            throw new FieldAlreadyUsed("email");
        }

        // Valider l'identifiant seulement s'il est fourni et différent de l'actuel
        if (updateDTO.getIdentifiant() != null && !updateDTO.getIdentifiant().trim().isEmpty() &&
            !updateDTO.getIdentifiant().equals(profil.getIdentifiant()) &&
            profileRepository.existsByIdentifiantAndIdNot(updateDTO.getIdentifiant(), profilId)) {
            throw new FieldAlreadyUsed("identifiant");
        }

        profileMapper.updateEntityFromUpdateDto(updateDTO, profil);

        // Ne mettre à jour le mot de passe que s'il est fourni et non vide
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            profil.setPasswordHash(hashPassword(updateDTO.getPassword()));
        }

        // Handle profile picture if provided (base64)
        if (updateDTO.getProfilPicture() != null && !updateDTO.getProfilPicture().trim().isEmpty()) {
            // Delete old picture if exists
            if (profil.getProfilPicture() != null && !profil.getProfilPicture().trim().isEmpty()) {
                pictureService.deleteImage(profil.getProfilPicture());
            }
            String picturePath = pictureService.saveProfilPicture(profil, updateDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return Optional.of(profileRepository.save(profil));
    }

    /**
     * Delete any profile (HyperAdmin only)
     */
    @Transactional
    public boolean deleteByHyperAdmin(UUID uuid) {
        Optional<Profil> optProfil = profileRepository.findById(uuid);
        if (optProfil.isEmpty()) {
            return false;
        }
        Profil profil = optProfil.get();
        profil.setDeleted(true);
        profileRepository.save(profil);
        return true;
    }

    /**
     * Find any profile by ID (HyperAdmin only)
     */
    @Transactional(readOnly = true)
    public Optional<Profil> findProfileByIdForHyperAdmin(UUID id) {
        return profileRepository.findById(id);
    }

    /**
     * Update profile from mobile app (only specific fields: profilPicture, birthday, firstName, lastName, email)
     */
    @Transactional
    public Profil updateProfilFromMobile(Profil profil, ProfilMobileUpdateDTO updateDTO) {
        // Validate email if provided and different from current
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()
            && !updateDTO.getEmail().equals(profil.getEmail())
            && profileRepository.existsByEmailAndIdNot(updateDTO.getEmail(), profil.getId())) {
            throw new FieldAlreadyUsed("email");
        }

        // Update firstName if provided
        if (updateDTO.getFirstName() != null) {
            profil.setFirstName(updateDTO.getFirstName());
        }

        // Update lastName if provided
        if (updateDTO.getLastName() != null) {
            profil.setLastName(updateDTO.getLastName());
        }

        // Update birthday if provided
        if (updateDTO.getBirthday() != null) {
            profil.setBirthday(updateDTO.getBirthday());
        }

        // Update email if provided
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            profil.setEmail(updateDTO.getEmail());
        }

        // Handle profile picture - save base64 image and update path
        if (updateDTO.getProfilPicture() != null && !updateDTO.getProfilPicture().trim().isEmpty()) {
            // Delete old picture if exists
            if (profil.getProfilPicture() != null && !profil.getProfilPicture().trim().isEmpty()) {
                pictureService.deleteImage(profil.getProfilPicture());
            }
            // Save new picture
            String picturePath = pictureService.saveProfilPicture(profil, updateDTO.getProfilPicture());
            profil.setProfilPicture(picturePath);
        }

        return profileRepository.save(profil);
    }
} 