package bzh.stack.apimovix.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.dto.common.EmailResponseDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.AccountService;
import bzh.stack.apimovix.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email", description = "API pour l'envoi d'emails")
public class EmailController {

    private final EmailService emailService;
    private final AccountService accountService;

    @PostMapping("/test")
    @AdminRequired
    @Operation(summary = "Test d'envoi d'email", description = "Envoie un email de test à l'adresse email du profil connecté (Admin requis)")
    public ResponseEntity<EmailResponseDTO> testEmail(HttpServletRequest request) {
        try {
            // Récupérer le profil de l'utilisateur connecté
            Profil profil = (Profil) request.getAttribute("profil");
            if (profil == null || profil.getAccount() == null) {
                return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                        .status("ERROR")
                        .message("Profil utilisateur non trouvé")
                        .build());
            }

            // Vérifier que l'email du profil est configuré
            if (profil.getEmail() == null || profil.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                        .status("ERROR")
                        .message("Email du profil non configuré")
                        .build());
            }

            // Récupérer les détails du compte avec la configuration SMTP
            Account account = accountService.findAccountById(profil.getAccount().getId())
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

            String email = profil.getEmail().trim();
            String title = "Test d'envoi d'email - Movix API";
            String message = """
                <p>Bonjour %s,</p>
                
                <p>Ceci est un email de test envoyé automatiquement par l'application Movix.</p>
                
                <p>Si vous recevez cet email, cela signifie que la configuration SMTP fonctionne correctement.</p>
                
                <p><strong>Date d'envoi :</strong> %s</p>
                
                <p><strong>Fonctionnalités testées :</strong></p>
                <ul>
                    <li>Configuration SMTP</li>
                    <li>Envoi d'emails HTML</li>
                </ul>
                
                <p><strong>Configuration utilisée :</strong></p>
                <ul>
                    <li>Compte : %s</li>
                    <li>SMTP personnalisé : %s</li>
                    %s
                </ul>
                
                <p>Cordialement,<br>
                L'équipe Movix</p>
                """.formatted(
                    profil.getFirstName() != null ? profil.getFirstName() : "Utilisateur",
                    LocalDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    account.getSociete() != null ? account.getSociete() : "Non défini",
                    account.getSmtpEnable() != null && account.getSmtpEnable() ? "Activé" : "Désactivé (SMTP par défaut)",
                    account.getSmtpEnable() != null && account.getSmtpEnable() ? 
                        String.format("<li>Host SMTP : %s</li>", account.getSmtpHost() != null ? account.getSmtpHost() : "Non défini") : ""
                );
            
            if (account.getSmtpEnable() != null && account.getSmtpEnable()) {
                if (account.getSmtpHost() == null || account.getSmtpHost().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                            .status("ERROR")
                            .message("SMTP activé mais host non configuré")
                            .build());
                }
                if (account.getSmtpPort() == null) {
                    return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                            .status("ERROR")
                            .message("SMTP activé mais port non configuré")
                            .build());
                }
                if (account.getSmtpUsername() == null || account.getSmtpUsername().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                            .status("ERROR")
                            .message("SMTP activé mais nom d'utilisateur non configuré")
                            .build());
                }
                if (account.getSmtpPassword() == null || account.getSmtpPassword().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                            .status("ERROR")
                            .message("SMTP activé mais mot de passe non configuré")
                            .build());
                }
                
                emailService.sendNotificationEmailWithCustomSmtp(email, title, message, account);
            } else {
                emailService.sendNotificationEmail(email, title, message);
            }
            
            return ResponseEntity.ok(EmailResponseDTO.builder()
                    .status("SUCCESS")
                    .message("Email de test envoyé avec succès à : " + email)
                    .build());
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de test : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(EmailResponseDTO.builder()
                    .status("ERROR")
                    .message("Erreur lors de l'envoi de l'email : " + e.getMessage())
                    .build());
        }
    }
} 