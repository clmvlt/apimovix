package bzh.stack.apimovix.dto.notification;

import bzh.stack.apimovix.model.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {

    private UUID accountId;
    private NotificationType type;
    private String title;
    private String message;
    private String relatedEntityType;
    private UUID relatedEntityId;
}
