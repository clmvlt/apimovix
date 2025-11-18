package bzh.stack.apimovix.dto.notification;

import bzh.stack.apimovix.model.Notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification data transfer object")
public class NotificationDTO {

    @Schema(description = "Unique identifier of the notification", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ID of the account that owns this notification", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID accountId;

    @Schema(description = "Type of notification", example = "ANOMALIE", allowableValues = {"ANOMALIE", "INFORMATION", "OTHER"})
    private NotificationType type;

    @Schema(description = "Notification title", example = "Nouvelle anomalie détectée")
    private String title;

    @Schema(description = "Notification message content", example = "Une anomalie a été détectée sur la commande #12345")
    private String message;

    @Schema(description = "Indicates if the notification has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "Timestamp when the notification was created", example = "2025-11-18T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the notification was marked as read (null if unread)", example = "2025-11-18T16:00:00", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "Type of the related entity (e.g., 'ANOMALIE', 'COMMAND')", example = "ANOMALIE", nullable = true)
    private String relatedEntityType;

    @Schema(description = "ID of the related entity", example = "123e4567-e89b-12d3-a456-426614174000", nullable = true)
    private UUID relatedEntityId;
}
