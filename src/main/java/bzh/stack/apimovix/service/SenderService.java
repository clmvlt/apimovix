package bzh.stack.apimovix.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.importer.SenderDTO;
import bzh.stack.apimovix.dto.sender.SenderCreateDTO;
import bzh.stack.apimovix.dto.sender.SenderUpdateDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.SenderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SenderService {
    private final SenderRepository senderRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<Sender> findAllSenders() {
        return senderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Sender> findSender(String code) {
        return senderRepository.findById(code);
    }

    @Transactional(readOnly = true)
    public Optional<Sender> findFirstSenderByAccount(Account account) {
        return senderRepository.findFirstByAccount(account);
    }

    @Transactional
    public Sender createSender(SenderDTO senderDTO) {
        Optional<Sender> existing = findSender(senderDTO.getCode());
        if (existing.isPresent()) {
            return existing.get();
        }
        Sender sender = new Sender();
        sender.mapFromDTO(senderDTO);
        return senderRepository.save(sender);
    }

    @Transactional
    public Sender createSenderFromDTO(SenderCreateDTO senderCreateDTO) {
        // Check if sender already exists
        Optional<Sender> existing = findSender(senderCreateDTO.getCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Sender with code " + senderCreateDTO.getCode() + " already exists");
        }

        // Find account
        UUID accountId = UUID.fromString(senderCreateDTO.getAccountId());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Create sender
        Sender sender = new Sender();
        sender.setCode(senderCreateDTO.getCode());
        sender.setName(senderCreateDTO.getName());
        sender.setAddress1(senderCreateDTO.getAddress1());
        sender.setAddress2(senderCreateDTO.getAddress2());
        sender.setAddress3(senderCreateDTO.getAddress3());
        sender.setPostalCode(senderCreateDTO.getPostalCode());
        sender.setCity(senderCreateDTO.getCity());
        sender.setCountry(senderCreateDTO.getCountry());
        sender.setInformations(senderCreateDTO.getInformations());
        sender.setQuality(senderCreateDTO.getQuality());
        sender.setFirstName(senderCreateDTO.getFirstName());
        sender.setLastName(senderCreateDTO.getLastName());
        sender.setEmail(senderCreateDTO.getEmail());
        sender.setPhone(senderCreateDTO.getPhone());
        sender.setFax(senderCreateDTO.getFax());
        sender.setLatitude(senderCreateDTO.getLatitude());
        sender.setLongitude(senderCreateDTO.getLongitude());
        sender.setAccount(account);

        return senderRepository.save(sender);
    }

    @Transactional
    public Optional<Sender> updateSender(String code, SenderUpdateDTO senderUpdateDTO) {
        Optional<Sender> optSender = findSender(code);
        if (optSender.isEmpty()) {
            return Optional.empty();
        }

        Sender sender = optSender.get();

        // Update fields if provided
        if (senderUpdateDTO.getName() != null) {
            sender.setName(senderUpdateDTO.getName());
        }
        if (senderUpdateDTO.getAddress1() != null) {
            sender.setAddress1(senderUpdateDTO.getAddress1());
        }
        if (senderUpdateDTO.getAddress2() != null) {
            sender.setAddress2(senderUpdateDTO.getAddress2());
        }
        if (senderUpdateDTO.getAddress3() != null) {
            sender.setAddress3(senderUpdateDTO.getAddress3());
        }
        if (senderUpdateDTO.getPostalCode() != null) {
            sender.setPostalCode(senderUpdateDTO.getPostalCode());
        }
        if (senderUpdateDTO.getCity() != null) {
            sender.setCity(senderUpdateDTO.getCity());
        }
        if (senderUpdateDTO.getCountry() != null) {
            sender.setCountry(senderUpdateDTO.getCountry());
        }
        if (senderUpdateDTO.getInformations() != null) {
            sender.setInformations(senderUpdateDTO.getInformations());
        }
        if (senderUpdateDTO.getQuality() != null) {
            sender.setQuality(senderUpdateDTO.getQuality());
        }
        if (senderUpdateDTO.getFirstName() != null) {
            sender.setFirstName(senderUpdateDTO.getFirstName());
        }
        if (senderUpdateDTO.getLastName() != null) {
            sender.setLastName(senderUpdateDTO.getLastName());
        }
        if (senderUpdateDTO.getEmail() != null) {
            sender.setEmail(senderUpdateDTO.getEmail());
        }
        if (senderUpdateDTO.getPhone() != null) {
            sender.setPhone(senderUpdateDTO.getPhone());
        }
        if (senderUpdateDTO.getFax() != null) {
            sender.setFax(senderUpdateDTO.getFax());
        }
        if (senderUpdateDTO.getLatitude() != null) {
            sender.setLatitude(senderUpdateDTO.getLatitude());
        }
        if (senderUpdateDTO.getLongitude() != null) {
            sender.setLongitude(senderUpdateDTO.getLongitude());
        }

        // Update account if provided
        if (senderUpdateDTO.getAccountId() != null) {
            UUID accountId = UUID.fromString(senderUpdateDTO.getAccountId());
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            sender.setAccount(account);
        }

        return Optional.of(senderRepository.save(sender));
    }

    @Transactional
    public boolean deleteSender(String code) {
        Optional<Sender> optSender = findSender(code);
        if (optSender.isEmpty()) {
            return false;
        }

        try {
            senderRepository.delete(optSender.get());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
