package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.ultronsport.domain.EvidenceContext;
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
import za.co.ultronsport.service.impl.NotificationServiceImpl;
import za.co.ultronsport.web.dto.NotificationSearchCriteria;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void notificationIsCreatedWithUnreadStatus() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = notificationService.createNotification(1L, NotificationType.SYSTEM,
                "System notice", "Hello", NotificationTargetType.SYSTEM, 99L);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    void userCanListOwnNotifications() {
        Notification notification = notification(1L);
        when(notificationRepository.findByRecipientUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        Page<Notification> page = notificationService.getMyNotifications(1L, criteria(null));

        assertThat(page.getContent()).containsExactly(notification);
    }

    @Test
    void userCannotAccessAnotherUsersNotification() {
        Notification notification = notification(2L);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userCanMarkOwnNotificationAsRead() {
        Notification notification = notification(1L);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification read = notificationService.markAsRead(1L, 5L);

        assertThat(read.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(read.getReadAt()).isNotNull();
    }

    @Test
    void userCanMarkAllOwnNotificationsAsRead() {
        Notification first = notification(1L);
        Notification second = notification(1L);
        when(notificationRepository.findByRecipientUserIdAndStatus(1L, NotificationStatus.UNREAD))
                .thenReturn(List.of(first, second));

        int marked = notificationService.markAllMineAsRead(1L);

        assertThat(marked).isEqualTo(2);
        assertThat(first.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(second.getStatus()).isEqualTo(NotificationStatus.READ);
        verify(notificationRepository).saveAll(List.of(first, second));
    }

    @Test
    void unreadCountReturnsCorrectCount() {
        when(notificationRepository.countByRecipientUserIdAndStatus(1L, NotificationStatus.UNREAD)).thenReturn(3L);

        assertThat(notificationService.countMyUnreadNotifications(1L)).isEqualTo(3);
    }

    @Test
    void evidenceSubmissionCreatesAdminNotificationFallback() {
        User admin = user("Admin", UserRole.ADMIN);
        setId(admin, 99L);
        EvidenceUpload evidence = evidence();
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of(admin));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyEvidenceSubmitted(evidence);

        Notification saved = capturedNotification();
        assertThat(saved.getRecipientUserId()).isEqualTo(99L);
        assertThat(saved.getType()).isEqualTo(NotificationType.EVIDENCE_SUBMITTED);
    }

    @Test
    void evidenceVerificationCreatesAthleteNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyEvidenceVerified(evidence());

        Notification saved = capturedNotification();
        assertThat(saved.getRecipientUserId()).isEqualTo(1L);
        assertThat(saved.getType()).isEqualTo(NotificationType.EVIDENCE_VERIFIED);
        assertThat(saved.getTargetType()).isEqualTo(NotificationTargetType.EVIDENCE);
    }

    @Test
    void evidenceRejectionCreatesAthleteNotificationWithReason() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyEvidenceRejected(evidence(), "Video is unclear");

        Notification saved = capturedNotification();
        assertThat(saved.getType()).isEqualTo(NotificationType.EVIDENCE_REJECTED);
        assertThat(saved.getMessage()).contains("Video is unclear");
    }

    @Test
    void evidenceFlagCreatesAthleteNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyEvidenceFlagged(evidence(), "Duplicate");

        assertThat(capturedNotification().getType()).isEqualTo(NotificationType.EVIDENCE_FLAGGED);
    }

    @Test
    void evidenceArchiveCreatesAthleteNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyEvidenceArchived(evidence());

        assertThat(capturedNotification().getType()).isEqualTo(NotificationType.EVIDENCE_ARCHIVED);
    }

    @Test
    void levelPlayScoreChangeCreatesAthleteNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyLevelPlayScoreChanged(1L, 11L, 20, 55, LevelPlayTier.BRONZE,
                LevelPlayTier.GOLD);

        Notification saved = capturedNotification();
        assertThat(saved.getType()).isEqualTo(NotificationType.LEVELPLAY_SCORE_CHANGED);
        assertThat(saved.getMessage()).contains("20 to 55", "BRONZE to GOLD");
    }

    private Notification capturedNotification() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        return captor.getValue();
    }

    private Notification notification(Long recipientUserId) {
        Notification notification = Notification.create(recipientUserId, NotificationType.SYSTEM, "System notice",
                "Hello", NotificationTargetType.SYSTEM, 99L, null);
        setId(notification, 5L);
        return notification;
    }

    private NotificationSearchCriteria criteria(String status) {
        return NotificationSearchCriteria.from(status, 0, 20, "createdAt", "DESC");
    }

    private EvidenceUpload evidence() {
        EvidenceUpload evidence = EvidenceUpload.createDraft(1L, 11L, "Goal highlight", "Cup final goal",
                "Football", "Striker", "Match highlight", EvidenceContext.MATCH, LocalDate.now(),
                null, "https://video.example/highlight");
        setId(evidence, 7L);
        return evidence;
    }

    private User user(String displayName, UserRole role) {
        User user = User.create(displayName, displayName.toLowerCase() + "@example.com", null, "hashed", role);
        user.activate();
        return user;
    }

    private void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
