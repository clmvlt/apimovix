package bzh.stack.apimovix.service.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.StatusType.CommandStatus;
import bzh.stack.apimovix.repository.command.CommandStatusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandStatusService {
    private final CommandStatusRepository commandStatusRepository;

    private final Map<Integer, CommandStatus> statusCache = new HashMap<>();

    @Transactional(readOnly = true)
    public Optional<CommandStatus> findCommandStatus(Integer id) {
        CommandStatus cachedStatus = statusCache.get(id);
        if (cachedStatus != null) {
            return Optional.of(cachedStatus);
        }

        CommandStatus status = commandStatusRepository.findById(id).orElse(null);
        if (status == null) {
            return Optional.empty();
        }

        statusCache.put(id, status);
        return Optional.of(status);
    }

    public void clearCache() {
        statusCache.clear();
    }
}
