package bzh.stack.apimovix.service.command;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.History.HistoryCommandStatus;
import bzh.stack.apimovix.model.StatusType.CommandStatus;
import bzh.stack.apimovix.repository.command.HistoryCommandStatusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryCommandStatusService {
    private final HistoryCommandStatusRepository historyCommandStatusRepository;

    @Transactional
    public HistoryCommandStatus createHistoryCommandStatus(Command command, Profil profil, CommandStatus status) {
        return createHistoryCommandStatus(command, profil, status, LocalDateTime.now());
    }

    @Transactional
    public HistoryCommandStatus createHistoryCommandStatus(Command command, Profil profil, CommandStatus status, LocalDateTime createdAt) {
        HistoryCommandStatus historyCommandStatus = new HistoryCommandStatus();
        historyCommandStatus.setCommand(command);
        historyCommandStatus.setProfil(profil);
        historyCommandStatus.setId(UUID.randomUUID());
        historyCommandStatus.setStatus(status);
        historyCommandStatus.setCreatedAt(createdAt);
        return historyCommandStatusRepository.save(historyCommandStatus);
    }
} 