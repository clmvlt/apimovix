package bzh.stack.apimovix.service;

import bzh.stack.apimovix.dto.notification.NotificationCreateDTO;
import bzh.stack.apimovix.dto.notification.NotificationDTO;
import bzh.stack.apimovix.mapper.NotificationMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Notification;
import bzh.stack.apimovix.model.Notification.NotificationType;
import bzh.stack.apimovix.repository.AccountRepository;
import bzh.stack.apimovix.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Get all notifications for a specific account
     * Returns all unread notifications + max 10 read notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByAccountId(UUID accountId) {
        log.info("Fetching notifications for account: {}", accountId);

        // Get all unread notifications
        List<Notification> unreadNotifications = notificationRepository.findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(accountId);

        // Get read notifications (limit to 10)
        List<Notification> readNotifications = notificationRepository.findByAccountIdAndIsReadTrueOrderByCreatedAtDesc(accountId);
        List<Notification> limitedReadNotifications = readNotifications.stream()
                .limit(10)
                .collect(Collectors.toList());

        // Combine both lists
        List<Notification> allNotifications = new java.util.ArrayList<>();
        allNotifications.addAll(unreadNotifications);
        allNotifications.addAll(limitedReadNotifications);

        // Sort by createdAt descending to maintain order
        allNotifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

        log.info("Returning {} unread + {} read notifications for account: {}",
                unreadNotifications.size(), limitedReadNotifications.size(), accountId);

        return allNotifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a specific account
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsByAccountId(UUID accountId) {
        log.info("Fetching unread notifications for account: {}", accountId);
        List<Notification> notifications = notificationRepository.findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(accountId);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by type for a specific account
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByType(UUID accountId, NotificationType type) {
        log.info("Fetching notifications of type {} for account: {}", type, accountId);
        List<Notification> notifications = notificationRepository.findByAccountIdAndTypeOrderByCreatedAtDesc(accountId, type);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications for an account
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID accountId) {
        log.info("Counting unread notifications for account: {}", accountId);
        return notificationRepository.countByAccountIdAndIsReadFalse(accountId);
    }

    /**
     * Create a new notification
     */
    @Transactional
    public NotificationDTO createNotification(NotificationCreateDTO createDTO) {
        log.info("Creating notification for account: {}", createDTO.getAccountId());

        // Verify account exists
        Account account = accountRepository.findById(createDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + createDTO.getAccountId()));

        Notification notification = notificationMapper.toEntity(createDTO);
        notification.setAccount(account);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", saved.getId());

        return notificationMapper.toDTO(saved);
    }

    /**
     * Mark a single notification as read
     */
    @Transactional
    public NotificationDTO markAsRead(UUID notificationId, UUID accountId) {
        log.info("Marking notification as read: {} for account: {}", notificationId, accountId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        // Security check: verify the notification belongs to the account
        if (!notification.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Unauthorized: This notification does not belong to your account");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        }

        return notificationMapper.toDTO(notification);
    }

    /**
     * Mark all notifications as read for an account
     */
    @Transactional
    public int markAllAsRead(UUID accountId) {
        log.info("Marking all notifications as read for account: {}", accountId);
        int count = notificationRepository.markAllAsReadByAccountId(accountId);
        log.info("Marked {} notifications as read for account: {}", count, accountId);
        return count;
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(UUID notificationId, UUID accountId) {
        log.info("Deleting notification: {} for account: {}", notificationId, accountId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        // Security check: verify the notification belongs to the account
        if (!notification.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Unauthorized: This notification does not belong to your account");
        }

        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted: {}", notificationId);
    }

    /**
     * Helper method to create a notification quickly
     */
    @Transactional
    public void sendNotification(UUID accountId, NotificationType type, String title, String message) {
        NotificationCreateDTO createDTO = new NotificationCreateDTO();
        createDTO.setAccountId(accountId);
        createDTO.setType(type);
        createDTO.setTitle(title);
        createDTO.setMessage(message);
        createNotification(createDTO);
    }

    /**
     * Helper method to create a notification with related entity
     */
    @Transactional
    public void sendNotificationWithEntity(UUID accountId, NotificationType type, String title,
                                          String message, String entityType, UUID entityId) {
        NotificationCreateDTO createDTO = new NotificationCreateDTO();
        createDTO.setAccountId(accountId);
        createDTO.setType(type);
        createDTO.setTitle(title);
        createDTO.setMessage(message);
        createDTO.setRelatedEntityType(entityType);
        createDTO.setRelatedEntityId(entityId);
        createNotification(createDTO);
    }
}
