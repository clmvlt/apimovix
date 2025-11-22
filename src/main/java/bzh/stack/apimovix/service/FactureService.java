package bzh.stack.apimovix.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.facture.FactureCreateDTO;
import bzh.stack.apimovix.dto.facture.FactureDTO;
import bzh.stack.apimovix.mapper.FactureMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Facture;
import bzh.stack.apimovix.model.Notification.NotificationType;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureService {

    private final FactureRepository factureRepository;
    private final AccountRepository accountRepository;
    private final FactureMapper factureMapper;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:D:uploads}")
    private String uploadDir;

    /**
     * Get all factures for a specific account ordered by date descending
     */
    @Transactional(readOnly = true)
    public List<FactureDTO> getFacturesByAccountId(UUID accountId) {
        log.info("Fetching factures for account: {}", accountId);
        List<Facture> factures = factureRepository.findByAccountIdOrderByDateFactureDesc(accountId);
        return factures.stream()
                .map(factureMapper::toDto)
                .toList();
    }

    /**
     * Get a facture by ID
     */
    @Transactional(readOnly = true)
    public Facture getFactureById(UUID factureId) {
        return factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée: " + factureId));
    }

    /**
     * Create a new facture with PDF in base64
     */
    @Transactional
    public FactureDTO createFacture(FactureCreateDTO createDTO) {
        log.info("Creating facture for account: {}", createDTO.getAccountId());

        Account account = accountRepository.findById(createDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account non trouvé: " + createDTO.getAccountId()));

        Facture facture = new Facture();
        facture.setDateFacture(createDTO.getDateFacture());
        facture.setMontantTTC(createDTO.getMontantTTC());
        facture.setIsPaid(createDTO.getIsPaid() != null ? createDTO.getIsPaid() : false);
        facture.setAccount(account);

        // Save PDF file from base64
        if (createDTO.getPdfBase64() != null && !createDTO.getPdfBase64().isEmpty()) {
            String pdfPath = savePdfFromBase64(createDTO.getPdfBase64(), account.getId());
            facture.setPdfPath(pdfPath);
        }

        Facture saved = factureRepository.save(facture);
        log.info("Facture created with ID: {}", saved.getId());

        // Create notification for the account
        String formattedDate = createDTO.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        notificationService.sendNotificationWithEntity(
                account.getId(),
                NotificationType.FACTURE,
                "Nouvelle facture disponible",
                "Une nouvelle facture du " + formattedDate + " d'un montant de " + createDTO.getMontantTTC() + " TTC est disponible.",
                "FACTURE",
                saved.getId()
        );

        return factureMapper.toDto(saved);
    }

    /**
     * Update an existing facture
     */
    @Transactional
    public FactureDTO updateFacture(UUID factureId, bzh.stack.apimovix.dto.facture.FactureUpdateDTO updateDTO) {
        log.info("Updating facture: {}", factureId);

        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée: " + factureId));

        if (updateDTO.getDateFacture() != null) {
            facture.setDateFacture(updateDTO.getDateFacture());
        }

        if (updateDTO.getMontantTTC() != null) {
            facture.setMontantTTC(updateDTO.getMontantTTC());
        }

        if (updateDTO.getIsPaid() != null) {
            facture.setIsPaid(updateDTO.getIsPaid());
        }

        // Update PDF if provided
        if (updateDTO.getPdfBase64() != null && !updateDTO.getPdfBase64().isEmpty()) {
            // Delete old PDF if exists
            if (facture.getPdfPath() != null) {
                deletePdfFile(facture.getPdfPath());
            }
            String pdfPath = savePdfFromBase64(updateDTO.getPdfBase64(), facture.getAccount().getId());
            facture.setPdfPath(pdfPath);
        }

        Facture saved = factureRepository.save(facture);
        log.info("Facture updated: {}", factureId);

        return factureMapper.toDto(saved);
    }

    /**
     * Delete a facture
     */
    @Transactional
    public void deleteFacture(UUID factureId) {
        log.info("Deleting facture: {}", factureId);

        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée: " + factureId));

        // Delete PDF file if exists
        if (facture.getPdfPath() != null) {
            deletePdfFile(facture.getPdfPath());
        }

        factureRepository.delete(facture);
        log.info("Facture deleted: {}", factureId);
    }

    /**
     * Get PDF file for a facture
     */
    public File getPdfFile(UUID factureId) {
        Facture facture = getFactureById(factureId);
        if (facture.getPdfPath() == null) {
            throw new RuntimeException("Aucun PDF associé à cette facture");
        }

        String fullPath = uploadDir + File.separator + facture.getPdfPath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new RuntimeException("Fichier PDF non trouvé");
        }
        return file;
    }

    /**
     * Save PDF file from base64 string
     */
    private String savePdfFromBase64(String base64, UUID accountId) {
        // Remove data:application/pdf;base64, prefix if present
        String cleanBase64 = base64;
        if (base64.contains(",")) {
            cleanBase64 = base64.substring(base64.indexOf(",") + 1);
        }

        byte[] pdfBytes;
        try {
            pdfBytes = Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 string", e);
            throw new RuntimeException("Le format base64 du PDF est invalide");
        }

        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String path = "factures" + File.separator + accountId + File.separator + dateFolder;
        String fullPath = uploadDir + File.separator + path;

        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "facture_" + UUID.randomUUID().toString() + ".pdf";
        String filePath = fullPath + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
            return path + File.separator + fileName;
        } catch (IOException e) {
            log.error("Error saving PDF file", e);
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier PDF");
        }
    }

    /**
     * Delete PDF file from disk
     */
    private void deletePdfFile(String pdfPath) {
        File file = new File(uploadDir + File.separator + pdfPath);
        if (file.exists()) {
            if (file.delete()) {
                log.info("PDF file deleted: {}", pdfPath);
                // Clean up empty parent directories
                File parentDir = file.getParentFile();
                while (parentDir != null && parentDir.isDirectory() && parentDir.list().length == 0) {
                    parentDir.delete();
                    parentDir = parentDir.getParentFile();
                }
            }
        }
    }
}
