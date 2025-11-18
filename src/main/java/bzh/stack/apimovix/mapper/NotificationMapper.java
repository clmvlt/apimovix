package bzh.stack.apimovix.mapper;

import bzh.stack.apimovix.dto.notification.NotificationCreateDTO;
import bzh.stack.apimovix.dto.notification.NotificationDTO;
import bzh.stack.apimovix.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "account.id", target = "accountId")
    NotificationDTO toDTO(Notification notification);

    @Mapping(source = "accountId", target = "account.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    Notification toEntity(NotificationCreateDTO createDTO);
}
