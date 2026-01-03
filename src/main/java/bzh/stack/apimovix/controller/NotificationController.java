package bzh.stack.apimovix.controller;

import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.notification.NotificationCreateDTO;
import bzh.stack.apimovix.dto.notification.NotificationDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.service.NotificationService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@TokenRequired
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "Get all notifications",
            description = "Retrieve all notifications for the authenticated account, ordered by creation date (newest first)"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = NotificationDTO.class))))
    @ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
    @ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        Account account = profil.getAccount();

        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByAccountId(account.getId());
            return MAPIR.ok(notifications);
        } catch (Exception e) {
            log.error("Error fetching notifications for account {}: {}", account.getId(), e.getMessage(), e);
            return MAPIR.badRequest("Failed to retrieve notifications: " + e.getMessage());
        }
    }

    @PutMapping("/{notificationId}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a single notification as read. Only the owner of the notification can mark it as read."
    )
    @ApiResponse(responseCode = "200", description = "Notification successfully marked as read",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationDTO.class)))
    @ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
    @ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
    public ResponseEntity<?> readNotification(
            HttpServletRequest request,
            @Parameter(description = "Notification ID", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID notificationId) {
        Profil profil = (Profil) request.getAttribute("profil");
        Account account = profil.getAccount();
        log.info("PUT /notifications/{}/read - account: {}", notificationId, account.getId());

        try {
            NotificationDTO notification = notificationService.markAsRead(notificationId, account.getId());
            return MAPIR.ok(notification);
        } catch (Exception e) {
            log.error("Error marking notification {} as read: {}", notificationId, e.getMessage(), e);
            return MAPIR.badRequest("Failed to mark notification as read: " + e.getMessage());
        }
    }

    @PutMapping("/read-all")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all unread notifications as read for the authenticated account. Returns the count of notifications that were marked as read."
    )
    @ApiResponse(responseCode = "200", description = "All notifications successfully marked as read",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = "{\"markedCount\": 5}")))
    @ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
    @ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
    public ResponseEntity<?> readAllNotifications(HttpServletRequest request) {
        Profil profil = (Profil) request.getAttribute("profil");
        Account account = profil.getAccount();
        log.info("PUT /notifications/read-all - account: {}", account.getId());

        try {
            int count = notificationService.markAllAsRead(account.getId());
            return MAPIR.ok(Map.of("markedCount", count));
        } catch (Exception e) {
            log.error("Error marking all notifications as read for account {}: {}", account.getId(), e.getMessage(), e);
            return MAPIR.badRequest("Failed to mark all notifications as read: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @HyperAdminRequired
    @Operation(
            summary = "[HyperAdmin] Create a notification for an account",
            description = "Create a new notification for a specific account. This endpoint is restricted to HyperAdmin users only."
    )
    @ApiResponse(responseCode = "200", description = "Notification successfully created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationDTO.class)))
    @ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
    @ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
    public ResponseEntity<?> createNotification(@Valid @RequestBody NotificationCreateDTO createDTO) {
        log.info("POST /notifications/create - accountId: {}, type: {}, title: {}",
                createDTO.getAccountId(), createDTO.getType(), createDTO.getTitle());

        try {
            NotificationDTO notification = notificationService.createNotification(createDTO);
            return MAPIR.ok(notification);
        } catch (Exception e) {
            log.error("Error creating notification for account {}: {}", createDTO.getAccountId(), e.getMessage(), e);
            return MAPIR.badRequest("Failed to create notification: " + e.getMessage());
        }
    }
}
