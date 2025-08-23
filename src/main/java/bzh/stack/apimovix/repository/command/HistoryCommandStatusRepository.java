package bzh.stack.apimovix.repository.command;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.History.HistoryCommandStatus;

@Repository
public interface HistoryCommandStatusRepository extends JpaRepository<HistoryCommandStatus, UUID> {
} 