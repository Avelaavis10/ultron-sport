package za.co.ultronsport.service;

import org.springframework.data.domain.Page;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationTargetType;
import za.co.ultronsport.domain.NotificationType;
import za.co.ultronsport.web.dto.NotificationSearchCriteria;

public interface NotificationService {
    Notification createNotification(Long recipientUserId, NotificationType type, String title, String message,
                                    NotificationTargetType targetType, Long targetId);

    Notification createNotification(Long recipientUserId, NotificationType type, String title, String message,
                                    NotificationTargetType targetType, Long targetId, String metadataJson);

    void notifyEvidenceSubmitted(EvidenceUpload evidence);

    void notifyEvidenceVerified(EvidenceUpload evidence);

    void notifyEvidenceRejected(EvidenceUpload evidence, String reason);

    void notifyEvidenceFlagged(EvidenceUpload evidence, String reason);

    void notifyEvidenceArchived(EvidenceUpload evidence);

    void notifyLevelPlayScoreChanged(Long athleteUserId, Long athleteProfileId, Integer oldScore, Integer newScore,
                                     LevelPlayTier oldTier, LevelPlayTier newTier);

    void notifyAchievementCreated(Long athleteUserId, Long achievementId);

    void notifyAthleteProfileUpdated(Long athleteUserId, Long athleteProfileId);

    void notifyOrganisationLinked(Long athleteUserId, Long athleteProfileId, Long organisationId);

    void notifyCoachProfileUpdated(Long coachUserId, Long coachProfileId);

    Page<Notification> getMyNotifications(Long currentUserId, NotificationSearchCriteria criteria);

    Page<Notification> getMyUnreadNotifications(Long currentUserId, NotificationSearchCriteria criteria);

    long countMyUnreadNotifications(Long currentUserId);

    Notification markAsRead(Long currentUserId, Long notificationId);

    int markAllMineAsRead(Long currentUserId);
}
