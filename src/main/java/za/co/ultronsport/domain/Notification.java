package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient_user_id"),
        @Index(name = "idx_notifications_recipient_status", columnList = "recipient_user_id,status"),
        @Index(name = "idx_notifications_target", columnList = "target_type,target_id"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1200)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType;

    private Long targetId;
    private Instant readAt;

    @Column(length = 2000)
    private String metadataJson;

    protected Notification() {
    }

    private Notification(Long recipientUserId, NotificationType type, String title, String message,
                         NotificationTargetType targetType, Long targetId, String metadataJson) {
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.UNREAD;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadataJson = metadataJson;
    }

    public static Notification create(Long recipientUserId, NotificationType type, String title, String message,
                                      NotificationTargetType targetType, Long targetId, String metadataJson) {
        return new Notification(recipientUserId, type, title, message, targetType, targetId, metadataJson);
    }

    public void markRead() {
        if (status == NotificationStatus.READ) {
            return;
        }
        status = NotificationStatus.READ;
        readAt = Instant.now();
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public NotificationTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public String getMetadataJson() {
        return metadataJson;
    }
}
