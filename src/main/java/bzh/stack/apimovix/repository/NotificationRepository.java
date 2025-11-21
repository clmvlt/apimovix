package bzh.stack.apimovix.repository;

import bzh.stack.apimovix.model.Notification;
import bzh.stack.apimovix.model.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Find all notifications for a specific account
    List<Notification> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    // Find unread notifications for a specific account
    List<Notification> findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(UUID accountId);

    // Find notifications by type for a specific account
    List<Notification> findByAccountIdAndTypeOrderByCreatedAtDesc(UUID accountId, NotificationType type);

    // Count unread notifications for an account
    long countByAccountIdAndIsReadFalse(UUID accountId);

    // Find read notifications for a specific account
    List<Notification> findByAccountIdAndIsReadTrueOrderByCreatedAtDesc(UUID accountId);

    // Mark all notifications as read for an account
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.account.id = :accountId AND n.isRead = false")
    int markAllAsReadByAccountId(@Param("accountId") UUID accountId);
}
