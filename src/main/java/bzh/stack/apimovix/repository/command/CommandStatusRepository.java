package bzh.stack.apimovix.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.StatusType.CommandStatus;

@Repository
public interface CommandStatusRepository extends JpaRepository<CommandStatus, Integer> {
} 