package bzh.stack.apimovix.repository.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bzh.stack.apimovix.model.Picture.CommandPicture;

@Repository
public interface CommandPictureRepository extends JpaRepository<CommandPicture, UUID> {

    List<CommandPicture> findByCreatedAtBefore(LocalDateTime cutoffDate);

    long countByCreatedAtBefore(LocalDateTime cutoffDate);
} 