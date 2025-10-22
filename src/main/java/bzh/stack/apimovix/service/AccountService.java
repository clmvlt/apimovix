package bzh.stack.apimovix.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.account.AccountCreateDTO;
import bzh.stack.apimovix.dto.account.AccountUpdateDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    @Transactional(readOnly = true)
    public Optional<Account> findAccountById(UUID id) {
        return accountRepository.findById(id);
    }
    
    @Transactional
    public Account updateAccount(UUID accountId, AccountUpdateDTO updateDTO) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        // Mise à jour des champs autorisés (seulement si non-null)
        if (updateDTO.getSociete() != null) {
            account.setSociete(updateDTO.getSociete());
        }
        if (updateDTO.getAddress1() != null) {
            account.setAddress1(updateDTO.getAddress1());
        }
        if (updateDTO.getAddress2() != null) {
            account.setAddress2(updateDTO.getAddress2());
        }
        if (updateDTO.getLatitude() != null) {
            account.setLatitude(updateDTO.getLatitude());
        }
        if (updateDTO.getLongitude() != null) {
            account.setLongitude(updateDTO.getLongitude());
        }
        if (updateDTO.getMaxProfiles() != null) {
            account.setMaxProfiles(updateDTO.getMaxProfiles());
        }
        if (updateDTO.getIsActive() != null) {
            account.setIsActive(updateDTO.getIsActive());
        }
        // Toujours permettre la mise à jour d'anomaliesEmails (y compris pour le vider)
        // Si null ou chaîne vide/blanche, on met null en base
        String emails = updateDTO.getAnomaliesEmails();
        if (emails != null) {
            emails = emails.trim();
            account.setAnomaliesEmails(emails.isEmpty() ? null : emails);
        } else {
            // Permettre explicitement de vider avec null
            account.setAnomaliesEmails(null);
        }
        
        // Mise à jour de la configuration SMTP (seulement si non-null)
        if (updateDTO.getSmtpHost() != null) {
            account.setSmtpHost(updateDTO.getSmtpHost());
        }
        if (updateDTO.getSmtpPort() != null) {
            account.setSmtpPort(updateDTO.getSmtpPort());
        }
        if (updateDTO.getSmtpUsername() != null) {
            account.setSmtpUsername(updateDTO.getSmtpUsername());
        }
        if (updateDTO.getSmtpPassword() != null) {
            account.setSmtpPassword(updateDTO.getSmtpPassword());
        }
        if (updateDTO.getSmtpEnable() != null) {
            account.setSmtpEnable(updateDTO.getSmtpEnable());
        }
        if (updateDTO.getSmtpUseTls() != null) {
            account.setSmtpUseTls(updateDTO.getSmtpUseTls());
        }
        if (updateDTO.getSmtpUseSsl() != null) {
            account.setSmtpUseSsl(updateDTO.getSmtpUseSsl());
        }
        if (updateDTO.getIsScanCIP() != null) {
            account.setIsScanCIP(updateDTO.getIsScanCIP());
        }


        return accountRepository.save(account);
    }

    @Transactional
    public Account createAccount(AccountCreateDTO createDTO) {
        Account account = new Account();

        account.setSociete(createDTO.getSociete());
        account.setAddress1(createDTO.getAddress1());
        account.setAddress2(createDTO.getAddress2());
        account.setLatitude(createDTO.getLatitude());
        account.setLongitude(createDTO.getLongitude());
        account.setMaxProfiles(createDTO.getMaxProfiles() != null ? createDTO.getMaxProfiles() : 0);
        account.setAnomaliesEmails(createDTO.getAnomaliesEmails());
        account.setIsActive(createDTO.getIsActive() != null ? createDTO.getIsActive() : true);

        // Configuration SMTP
        account.setSmtpHost(createDTO.getSmtpHost());
        account.setSmtpPort(createDTO.getSmtpPort());
        account.setSmtpUsername(createDTO.getSmtpUsername());
        account.setSmtpPassword(createDTO.getSmtpPassword());
        account.setSmtpEnable(createDTO.getSmtpEnable() != null ? createDTO.getSmtpEnable() : false);
        account.setSmtpUseTls(createDTO.getSmtpUseTls() != null ? createDTO.getSmtpUseTls() : true);
        account.setSmtpUseSsl(createDTO.getSmtpUseSsl() != null ? createDTO.getSmtpUseSsl() : false);
        account.setIsScanCIP(createDTO.getIsScanCIP() != null ? createDTO.getIsScanCIP() : false);
        account.setAutoSendAnomalieEmails(createDTO.getAutoSendAnomalieEmails() != null ? createDTO.getAutoSendAnomalieEmails() : false);

        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccountById(UUID accountId, AccountUpdateDTO updateDTO) {
        return updateAccount(accountId, updateDTO);
    }

    @Transactional
    public void deleteAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        accountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
} 