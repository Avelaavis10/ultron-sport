package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.service.impl.AchievementServiceImpl;
import za.co.ultronsport.web.dto.CreateAchievementRequest;
import za.co.ultronsport.web.dto.UpdateAchievementRequest;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private LevelPlayScoreService levelPlayScoreService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Test
    void athleteCanCreateOwnAchievement() {
        CreateAchievementRequest request = new CreateAchievementRequest(1L, "Top Scorer", "League top scorer",
                LocalDate.now());
        when(athleteProfileRepository.findById(1L)).thenReturn(Optional.of(profile(7L)));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Achievement achievement = achievementService.create(7L, request);

        assertThat(achievement.getTitle()).isEqualTo("Top Scorer");
        verify(levelPlayScoreService).recalculateForAthlete(1L);
        verify(notificationService).notifyAchievementCreated(7L, null);
    }

    @Test
    void athleteCannotCreateAchievementForAnotherAthleteProfile() {
        CreateAchievementRequest request = new CreateAchievementRequest(1L, "Top Scorer", "League top scorer",
                LocalDate.now());
        when(athleteProfileRepository.findById(1L)).thenReturn(Optional.of(profile(9L)));

        assertThatThrownBy(() -> achievementService.create(7L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void athleteCanUpdateOwnAchievement() {
        Achievement achievement = achievement(1L);
        when(achievementRepository.findById(5L)).thenReturn(Optional.of(achievement));
        when(athleteProfileRepository.findById(1L)).thenReturn(Optional.of(profile(7L)));
        when(achievementRepository.save(achievement)).thenReturn(achievement);

        Achievement updated = achievementService.update(7L, 5L,
                new UpdateAchievementRequest("Updated", "Updated description", LocalDate.now()));

        assertThat(updated.getTitle()).isEqualTo("Updated");
        verify(levelPlayScoreService).recalculateForAthlete(1L);
    }

    @Test
    void athleteCannotUpdateAnotherAthletesAchievement() {
        Achievement achievement = achievement(1L);
        when(achievementRepository.findById(5L)).thenReturn(Optional.of(achievement));
        when(athleteProfileRepository.findById(1L)).thenReturn(Optional.of(profile(9L)));

        assertThatThrownBy(() -> achievementService.update(7L, 5L,
                new UpdateAchievementRequest("Updated", "Updated description", LocalDate.now())))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Achievement achievement(Long athleteProfileId) {
        Achievement achievement = Achievement.create(athleteProfileId, "Top Scorer", "League top scorer",
                LocalDate.now());
        ReflectionTestUtils.setField(achievement, "id", 5L);
        return achievement;
    }

    private AthleteProfile profile(Long userId) {
        return AthleteProfile.create(userId, "Football", "Striker", 18, "Male", "Cape Town",
                "CPUT FC", "Bio");
    }
}
