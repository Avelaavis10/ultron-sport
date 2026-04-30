package za.co.ultronsport.service.impl;

import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationStatus;
import za.co.ultronsport.domain.NotificationTargetType;
import za.co.ultronsport.domain.NotificationType;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.NotificationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.NotificationSearchCriteria;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(Long recipientUserId, NotificationType type, String title, String message,
                                           NotificationTargetType targetType, Long targetId) {
        return createNotification(recipientUserId, type, title, message, targetType, targetId, null);
    }

    @Override
    @Transactional
    public Notification createNotification(Long recipientUserId, NotificationType type, String title, String message,
                                           NotificationTargetType targetType, Long targetId, String metadataJson) {
        if (recipientUserId == null) {
            throw new InvalidStateException("Notification recipient is required.");
        }
        if (type == null) {
            throw new InvalidStateException("Notification type is required.");
        }
        if (!hasText(title)) {
            throw new InvalidStateException("Notification title is required.");
        }
        if (!hasText(message)) {
            throw new InvalidStateException("Notification message is required.");
        }
        if (targetType == null) {
            throw new InvalidStateException("Notification target type is required.");
        }
        Notification notification = Notification.create(recipientUserId, type, title.trim(), message.trim(),
                targetType, targetId, clean(metadataJson));
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyEvidenceSubmitted(EvidenceUpload evidence) {
        // TODO: Replace admin fallback with coach targeting once roster/team relationships are modelled.
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        admins.forEach(admin -> createNotification(admin.getId(), NotificationType.EVIDENCE_SUBMITTED,
                "Evidence submitted",
                "New evidence is waiting for verification: " + evidence.getTitle() + ".",
                NotificationTargetType.EVIDENCE, evidence.getId()));
    }

    @Override
    @Transactional
    public void notifyEvidenceVerified(EvidenceUpload evidence) {
        createNotification(evidence.getUploadedByUserId(), NotificationType.EVIDENCE_VERIFIED,
                "Evidence verified",
                "Your evidence was verified: " + evidence.getTitle() + ".",
                NotificationTargetType.EVIDENCE, evidence.getId());
    }

    @Override
    @Transactional
    public void notifyEvidenceRejected(EvidenceUpload evidence, String reason) {
        createNotification(evidence.getUploadedByUserId(), NotificationType.EVIDENCE_REJECTED,
                "Evidence rejected",
                "Your evidence was rejected: " + evidence.getTitle() + ". Reason: " + reason,
                NotificationTargetType.EVIDENCE, evidence.getId());
    }

    @Override
    @Transactional
    public void notifyEvidenceFlagged(EvidenceUpload evidence, String reason) {
        createNotification(evidence.getUploadedByUserId(), NotificationType.EVIDENCE_FLAGGED,
                "Evidence flagged",
                "Your evidence was flagged for moderation: " + evidence.getTitle() + ". Reason: " + reason,
                NotificationTargetType.EVIDENCE, evidence.getId());
    }

    @Override
    @Transactional
    public void notifyEvidenceArchived(EvidenceUpload evidence) {
        createNotification(evidence.getUploadedByUserId(), NotificationType.EVIDENCE_ARCHIVED,
                "Evidence archived",
                "Your evidence was archived: " + evidence.getTitle() + ".",
                NotificationTargetType.EVIDENCE, evidence.getId());
    }

    @Override
    @Transactional
    public void notifyLevelPlayScoreChanged(Long athleteUserId, Long athleteProfileId, Integer oldScore,
                                            Integer newScore, LevelPlayTier oldTier, LevelPlayTier newTier) {
        String message = "Your LevelPlay score changed from " + oldScore + " to " + newScore + ".";
        if (!Objects.equals(oldTier, newTier)) {
            message += " Your tier changed from " + oldTier + " to " + newTier + ".";
        }
        createNotification(athleteUserId, NotificationType.LEVELPLAY_SCORE_CHANGED,
                "LevelPlay score updated", message, NotificationTargetType.LEVELPLAY_SCORE, athleteProfileId);
    }

    @Override
    @Transactional
    public void notifyAchievementCreated(Long athleteUserId, Long achievementId) {
        createNotification(athleteUserId, NotificationType.ACHIEVEMENT_CREATED,
                "Achievement added",
                "Your achievement was added and may improve your LevelPlay score.",
                NotificationTargetType.ACHIEVEMENT, achievementId);
    }

    @Override
    @Transactional
    public void notifyAthleteProfileUpdated(Long athleteUserId, Long athleteProfileId) {
        createNotification(athleteUserId, NotificationType.ATHLETE_PROFILE_UPDATED,
                "Athlete profile updated",
                "Your athlete profile was updated.",
                NotificationTargetType.ATHLETE_PROFILE, athleteProfileId);
    }

    @Override
    @Transactional
    public void notifyOrganisationLinked(Long athleteUserId, Long athleteProfileId, Long organisationId) {
        createNotification(athleteUserId, NotificationType.ORGANISATION_LINKED,
                "Organisation link updated",
                "Your athlete profile organisation link was updated.",
                NotificationTargetType.ORGANISATION, organisationId);
    }

    @Override
    @Transactional
    public void notifyCoachProfileUpdated(Long coachUserId, Long coachProfileId) {
        createNotification(coachUserId, NotificationType.COACH_PROFILE_UPDATED,
                "Coach profile saved",
                "Your coach profile was saved.",
                NotificationTargetType.COACH_PROFILE, coachProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Long currentUserId, NotificationSearchCriteria criteria) {
        criteria.validate();
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(),
                Sort.by(criteria.direction(), criteria.effectiveSortBy()));
        if (criteria.status() == null) {
            return notificationRepository.findByRecipientUserId(currentUserId, pageRequest);
        }
        return notificationRepository.findByRecipientUserIdAndStatus(currentUserId, criteria.status(), pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getMyUnreadNotifications(Long currentUserId, NotificationSearchCriteria criteria) {
        return getMyNotifications(currentUserId, criteria.unreadOnly());
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnreadNotifications(Long currentUserId) {
        return notificationRepository.countByRecipientUserIdAndStatus(currentUserId, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long currentUserId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getRecipientUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own notifications.");
        }
        notification.markRead();
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public int markAllMineAsRead(Long currentUserId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndStatus(currentUserId,
                NotificationStatus.UNREAD);
        unread.forEach(Notification::markRead);
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    private String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
