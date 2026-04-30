package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationStatus;

public record MarkNotificationReadResponse(
        Long notificationId,
        NotificationStatus status,
        Instant readAt
) {
    public static MarkNotificationReadResponse from(Notification notification) {
        return new MarkNotificationReadResponse(notification.getId(), notification.getStatus(),
                notification.getReadAt());
    }
}
