package bzh.stack.apimovix.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (updateDTO.getAnomaliesEmails() != null) {
            account.setAnomaliesEmails(updateDTO.getAnomaliesEmails());
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

        
        return accountRepository.save(account);
    }
} 