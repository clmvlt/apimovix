package bzh.stack.apimovix.service;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import bzh.stack.apimovix.dto.common.EmailDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Constantes pour les couleurs et le branding Movix
    private static final String MOVIX_PRIMARY_COLOR = "#123456";
    private static final String MOVIX_LIGHT_BG = "#F8F9FA";
    private static final String MOVIX_DARK_TEXT = "#2C3E50";

    /**
     * Crée un JavaMailSender personnalisé avec les paramètres SMTP du compte
     */
    private JavaMailSender createCustomMailSender(Account account) {
        JavaMailSenderImpl customMailSender = new JavaMailSenderImpl();
        
        // Vérification des champs obligatoires
        if (account.getSmtpHost() == null || account.getSmtpHost().trim().isEmpty()) {
            throw new RuntimeException("Host SMTP non configuré");
        }
        if (account.getSmtpPort() == null) {
            throw new RuntimeException("Port SMTP non configuré");
        }
        if (account.getSmtpUsername() == null || account.getSmtpUsername().trim().isEmpty()) {
            throw new RuntimeException("Nom d'utilisateur SMTP non configuré");
        }
        if (account.getSmtpPassword() == null || account.getSmtpPassword().trim().isEmpty()) {
            throw new RuntimeException("Mot de passe SMTP non configuré");
        }
        
        // Configuration de base
        customMailSender.setHost(account.getSmtpHost().trim());
        customMailSender.setPort(account.getSmtpPort());
        customMailSender.setUsername(account.getSmtpUsername().trim());
        customMailSender.setPassword(account.getSmtpPassword());
        
        // Configuration des propriétés
        Properties props = customMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        
        if (account.getSmtpUseTls() != null && account.getSmtpUseTls()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        
        if (account.getSmtpUseSsl() != null && account.getSmtpUseSsl()) {
            props.put("mail.smtp.socketFactory.port", account.getSmtpPort());
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        
        return customMailSender;
    }

    /**
     * Génère le header HTML pour les emails Movix
     */
    private String generateMovixHeader() {
        return """
            <div style="background-color: %s; padding: 20px; text-align: center; border-bottom: 2px solid %s;">
                <h1 style="color: %s; margin: 0; font-size: 24px; font-weight: 600;">
                    Movix
                </h1>
            </div>
            """.formatted(MOVIX_LIGHT_BG, MOVIX_PRIMARY_COLOR, MOVIX_DARK_TEXT);
    }

    /**
     * Génère le footer HTML pour les emails Movix avec avertissement noreply
     */
    private String generateMovixFooter() {
        return """
            <div style="background-color: %s; padding: 15px; border-top: 1px solid #E9ECEF; margin-top: 20px;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <!-- Avertissement noreply simple -->
                    <p style="color: #856404; margin: 0 0 10px; font-size: 12px; background-color: #FFF3CD; padding: 8px; border-radius: 3px;">
                        ⚠️ Email automatique - Ne pas répondre
                    </p>
                    
                    <!-- Contact simple -->
                    <p style="color: %s; margin: 0 0 8px; font-size: 12px; text-align: center;">
                        Support : <a href="mailto:contact@stack.bzh" style="color: %s;">contact@stack.bzh</a>
                    </p>
                    
                    <!-- Copyright -->
                    <p style="color: #6C757D; margin: 0; font-size: 11px; text-align: center;">
                        © 2025 Movix
                    </p>
                </div>
            </div>
            """.formatted(MOVIX_LIGHT_BG, MOVIX_DARK_TEXT, MOVIX_PRIMARY_COLOR);
    }

    /**
     * Génère le contenu principal de l'email avec style Movix
     */
    private String generateMovixContent(String title, String message) {
        return """
            <div style="background-color: white; padding: 25px 20px;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: %s; margin: 0 0 15px; font-size: 20px; font-weight: 600;">
                        %s
                    </h2>
                    <div style="color: %s; font-size: 14px; line-height: 1.5;">
                        %s
                    </div>
                </div>
            </div>
            """.formatted(MOVIX_DARK_TEXT, title, MOVIX_DARK_TEXT, message);
    }

    /**
     * Envoie un email simple (texte)
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email HTML", e);
        }
    }

    /**
     * Envoie un email avec CC
     */
    public void sendEmailWithCc(String to, List<String> cc, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec CC", e);
        }
    }

    /**
     * Envoie un email avec BCC
     */
    public void sendEmailWithBcc(String to, List<String> bcc, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0]));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec BCC", e);
        }
    }

    /**
     * Envoie un email en utilisant le DTO EmailDTO
     */
    public void sendEmail(EmailDTO emailDTO) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(emailDTO.getTo());
            
            if (emailDTO.getCc() != null && !emailDTO.getCc().isEmpty()) {
                helper.setCc(emailDTO.getCc().toArray(new String[0]));
            }
            
            if (emailDTO.getBcc() != null && !emailDTO.getBcc().isEmpty()) {
                helper.setBcc(emailDTO.getBcc().toArray(new String[0]));
            }
            
            helper.setSubject(emailDTO.getSubject());
            helper.setText(emailDTO.getContent(), emailDTO.isHtml());
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email de notification générique avec design Movix
     */
    public void sendNotificationEmail(String to, String title, String message) {
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - Movix</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0; 
                        padding: 0; 
                        background-color: #f4f6f8;
                    }
                    .email-container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background-color: white; 
                        border-radius: 8px; 
                        box-shadow: 0 4px 20px rgba(0,0,0,0.1); 
                        overflow: hidden;
                    }
                    .content-wrapper {
                        background-color: white;
                    }
                    @media only screen and (max-width: 600px) {
                        .email-container { 
                            margin: 10px; 
                            border-radius: 4px; 
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    %s
                    <div class="content-wrapper">
                        %s
                    </div>
                    %s
                </div>
            </body>
            </html>
            """.formatted(title, generateMovixHeader(), generateMovixContent(title, message), generateMovixFooter());
        
        sendHtmlEmail(to, title, htmlContent);
    }

    /**
     * Envoie un email de notification avec contenu personnalisé et design Movix
     */
    public void sendCustomNotificationEmail(String to, String title, String message, String customContent) {
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - Movix</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0; 
                        padding: 0; 
                        background-color: #f4f6f8;
                    }
                    .email-container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background-color: white; 
                        border-radius: 8px; 
                        box-shadow: 0 4px 20px rgba(0,0,0,0.1); 
                        overflow: hidden;
                    }
                    .content-wrapper {
                        background-color: white;
                    }
                    @media only screen and (max-width: 600px) {
                        .email-container { 
                            margin: 10px; 
                            border-radius: 4px; 
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    %s
                    <div class="content-wrapper">
                        %s
                        %s
                    </div>
                    %s
                </div>
            </body>
            </html>
            """.formatted(title, generateMovixHeader(), generateMovixContent(title, message), customContent, generateMovixFooter());
        
        sendHtmlEmail(to, title, htmlContent);
    }

    /**
     * Envoie un email de notification avec SMTP personnalisé
     */
    public void sendNotificationEmailWithCustomSmtp(String to, String title, String message, Account account) {
        try {
            // Vérification des paramètres
            if (to == null || to.trim().isEmpty()) {
                throw new RuntimeException("Adresse email de destination manquante");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new RuntimeException("Titre de l'email manquant");
            }
            if (message == null) {
                message = "";
            }
            if (account == null) {
                throw new RuntimeException("Compte utilisateur manquant");
            }
            
            JavaMailSender customMailSender = createCustomMailSender(account);
            
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - Movix</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            line-height: 1.6; 
                            color: #333; 
                            margin: 0; 
                            padding: 0; 
                            background-color: #f4f6f8;
                        }
                        .email-container { 
                            max-width: 600px; 
                            margin: 20px auto; 
                            background-color: white; 
                            border-radius: 8px; 
                            box-shadow: 0 4px 20px rgba(0,0,0,0.1); 
                            overflow: hidden;
                        }
                        .content-wrapper {
                            background-color: white;
                        }
                        @media only screen and (max-width: 600px) {
                            .email-container { 
                                margin: 10px; 
                                border-radius: 4px; 
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        %s
                        <div class="content-wrapper">
                            %s
                        </div>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(title, generateMovixHeader(), generateMovixContent(title, message), generateMovixFooter());
            
            MimeMessage mimeMessage = customMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(account.getSmtpUsername().trim());
            helper.setTo(to.trim());
            helper.setSubject(title);
            helper.setText(htmlContent, true);
            
            customMailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec SMTP personnalisé : " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Envoie un email de notification d'anomalie avec le PDF en pièce jointe
     */
    public void sendAnomalieNotificationEmail(Anomalie anomalie, byte[] pdfBytes) {
        try {
            Account account = anomalie.getAccount();
            
            // Vérifier si des emails sont configurés pour les anomalies
            if (account.getAnomaliesEmails() == null || account.getAnomaliesEmails().trim().isEmpty()) {
                return;
            }
            
            // Diviser les emails (séparés par des virgules ou points-virgules)
            String[] emailList = account.getAnomaliesEmails().split("[;,]");
            
            String title = "Nouvelle anomalie signalée - " + anomalie.getTypeAnomalie().getName();
            String message = generateAnomalieEmailMessage(anomalie);
            
            // Envoyer l'email à chaque adresse configurée
            for (String email : emailList) {
                email = email.trim();
                if (!email.isEmpty()) {
                    try {
                        if (account.getSmtpEnable() != null && account.getSmtpEnable()) {
                            sendAnomalieEmailWithCustomSmtp(email, title, message, account, anomalie, pdfBytes);
                        } else {
                            sendAnomalieEmailWithDefaultSmtp(email, title, message, anomalie, pdfBytes);
                        }
                    } catch (Exception e) {
                        // Continuer avec les autres emails même si un échoue
                    }
                }
            }
            
        } catch (Exception e) {
            // Erreur silencieuse
        }
    }

    /**
     * Envoie un email d'anomalie avec SMTP personnalisé et PDF en pièce jointe
     */
    private void sendAnomalieEmailWithCustomSmtp(String to, String title, String message, Account account, Anomalie anomalie, byte[] pdfBytes) {
        try {
            JavaMailSender customMailSender = createCustomMailSender(account);
            
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - Movix</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            line-height: 1.6; 
                            color: #333; 
                            margin: 0; 
                            padding: 0; 
                            background-color: #f4f6f8;
                        }
                        .email-container { 
                            max-width: 600px; 
                            margin: 20px auto; 
                            background-color: white; 
                            border-radius: 8px; 
                            box-shadow: 0 4px 20px rgba(0,0,0,0.1); 
                            overflow: hidden;
                        }
                        .content-wrapper {
                            background-color: white;
                        }
                        @media only screen and (max-width: 600px) {
                            .email-container { 
                                margin: 10px; 
                                border-radius: 4px; 
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        %s
                        <div class="content-wrapper">
                            %s
                        </div>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(title, generateMovixHeader(), generateMovixContent(title, message), generateMovixFooter());
            
            MimeMessage mimeMessage = customMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(account.getSmtpUsername().trim());
            helper.setTo(to.trim());
            helper.setSubject(title);
            helper.setText(htmlContent, true);
            
            // Ajouter le PDF en pièce jointe
            String filename = "anomalie_" + anomalie.getId() + ".pdf";
            helper.addAttachment(filename, new jakarta.mail.util.ByteArrayDataSource(pdfBytes, "application/pdf"));
            
            customMailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'anomalie avec SMTP personnalisé : " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Envoie un email d'anomalie avec SMTP par défaut et PDF en pièce jointe
     */
    private void sendAnomalieEmailWithDefaultSmtp(String to, String title, String message, Anomalie anomalie, byte[] pdfBytes) {
        try {
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - Movix</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            line-height: 1.6; 
                            color: #333; 
                            margin: 0; 
                            padding: 0; 
                            background-color: #f4f6f8;
                        }
                        .email-container { 
                            max-width: 600px; 
                            margin: 20px auto; 
                            background-color: white; 
                            border-radius: 8px; 
                            box-shadow: 0 4px 20px rgba(0,0,0,0.1); 
                            overflow: hidden;
                        }
                        .content-wrapper {
                            background-color: white;
                        }
                        @media only screen and (max-width: 600px) {
                            .email-container { 
                                margin: 10px; 
                                border-radius: 4px; 
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        %s
                        <div class="content-wrapper">
                            %s
                        </div>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(title, generateMovixHeader(), generateMovixContent(title, message), generateMovixFooter());
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(title);
            helper.setText(htmlContent, true);
            
            // Ajouter le PDF en pièce jointe
            String filename = "anomalie_" + anomalie.getId() + ".pdf";
            helper.addAttachment(filename, new jakarta.mail.util.ByteArrayDataSource(pdfBytes, "application/pdf"));
            
            mailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'anomalie avec SMTP par défaut", e);
        }
    }

    /**
     * Génère le message HTML pour l'email d'anomalie
     */
    private String generateAnomalieEmailMessage(Anomalie anomalie) {
        StringBuilder message = new StringBuilder();
        
        message.append("<p><strong>Une nouvelle anomalie a été signalée dans votre système Movix.</strong></p>");
        message.append("<br>");
        
        message.append("<p><strong>Détails de l'anomalie :</strong></p>");
        message.append("<ul>");
        message.append("<li><strong>Type :</strong> ").append(anomalie.getTypeAnomalie().getName()).append("</li>");
        
        if (anomalie.getPharmacy() != null) {
            message.append("<li><strong>Pharmacie :</strong> ").append(anomalie.getPharmacy().getName()).append(" (").append(anomalie.getPharmacy().getCip()).append(")</li>");
            message.append("<li><strong>Adresse :</strong> ").append(anomalie.getPharmacy().getFullAdr()).append(", ").append(anomalie.getPharmacy().getFullCity()).append("</li>");
        }
        
        if (anomalie.getPackages() != null && !anomalie.getPackages().isEmpty()) {
            message.append("<li><strong>Nombre de colis concernés :</strong> ").append(anomalie.getPackages().size()).append("</li>");
        }
        
        if (anomalie.getOther() != null && !anomalie.getOther().trim().isEmpty()) {
            message.append("<li><strong>Description :</strong> ").append(anomalie.getOther()).append("</li>");
        }
        
        if (anomalie.getActions() != null && !anomalie.getActions().trim().isEmpty()) {
            message.append("<li><strong>Actions effectuée :</strong> ").append(anomalie.getActions()).append("</li>");
        }
        
        if (anomalie.getProfil() != null) {
            message.append("<li><strong>Signalé par :</strong> ").append(anomalie.getProfil().getFullName()).append("</li>");
        }
        
        message.append("<li><strong>Date de signalement :</strong> ").append(anomalie.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(" (heure de Paris)</li>");
        message.append("</ul>");
        
        message.append("<br>");
        message.append("<p>Le rapport détaillé de cette anomalie est joint à cet email au format PDF.</p>");
        
        return message.toString();
    }
} 