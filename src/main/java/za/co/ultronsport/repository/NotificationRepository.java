package za.co.ultronsport.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientUserId(Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status,
                                                      Pageable pageable);

    List<Notification> findByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);

    long countByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);
}
