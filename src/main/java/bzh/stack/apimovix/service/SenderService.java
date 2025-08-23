package bzh.stack.apimovix.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.importer.SenderDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Sender;
import bzh.stack.apimovix.repository.SenderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SenderService {
    private final SenderRepository senderRepository;

    @Transactional(readOnly = true)
    public Optional<Sender> findSender(String code) {
        return Optional.of(senderRepository.findById(code).orElse(null));
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
}
