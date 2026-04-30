package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationStatus;
import za.co.ultronsport.domain.NotificationTargetType;
import za.co.ultronsport.domain.NotificationType;

public record NotificationResponse(
        Long id,
        Long recipientUserId,
        NotificationType type,
        String title,
        String message,
        NotificationStatus status,
        NotificationTargetType targetType,
        Long targetId,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRecipientUserId(),
                notification.getType(), notification.getTitle(), notification.getMessage(), notification.getStatus(),
                notification.getTargetType(), notification.getTargetId(), notification.getCreatedAt(),
                notification.getReadAt());
    }
}
