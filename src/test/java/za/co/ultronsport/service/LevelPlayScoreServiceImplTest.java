package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.impl.LevelPlayScoreServiceImpl;
import za.co.ultronsport.web.dto.LevelPlayScoreExplanationResponse;

@ExtendWith(MockitoExtension.class)
class LevelPlayScoreServiceImplTest {

    @Mock
    private LevelPlayScoreRepository levelPlayScoreRepository;

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private EvidenceUploadRepository evidenceUploadRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminActionLogService adminActionLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LevelPlayScoreServiceImpl levelPlayScoreService;

    @Test
    void athleteWithNoEvidenceGetsLowScoreAndBronzeTier() {
        givenScoreInputs(9L, completeProfile(), 0, 0, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getFinalCredibilityScore()).isEqualTo(16);
        assertThat(score.getTier()).isEqualTo(LevelPlayTier.BRONZE);
    }

    @Test
    void athleteWithOneVerifiedEvidenceGetsCorrectEvidenceScore() {
        givenScoreInputs(9L, completeProfile(), 1, 0, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getVerifiedEvidenceCount()).isEqualTo(1);
        assertThat(score.getEvidenceScore()).isEqualTo(20);
    }

    @Test
    void athleteWithMultipleVerifiedEvidenceRecordsGetsCappedEvidenceScore() {
        givenScoreInputs(9L, completeProfile(), 8, 0, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getVerifiedEvidenceCount()).isEqualTo(8);
        assertThat(score.getEvidenceScore()).isEqualTo(60);
    }

    @Test
    void achievementScoreIsCalculatedCorrectly() {
        givenScoreInputs(9L, completeProfile(), 0, 3, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getAchievementCount()).isEqualTo(3);
        assertThat(score.getAchievementScore()).isEqualTo(20);
    }

    @Test
    void coachVerificationScoreIsCalculatedCorrectly() {
        givenScoreInputs(9L, completeProfile(), 0, 0, 2, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getCoachVerificationCount()).isEqualTo(2);
        assertThat(score.getVerificationScore()).isEqualTo(15);
    }

    @Test
    void profileCompletenessScoreIsCalculatedCorrectly() {
        AthleteProfile partialProfile = AthleteProfile.create(1L, "Football", "Striker", 18, null,
                null, null, null);
        givenScoreInputs(9L, partialProfile, 0, 0, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getProfileCompletenessScore()).isEqualTo(44);
        assertThat(score.getProfileCompletenessContribution()).isEqualTo(9);
    }

    @Test
    void profileCompletenessScoreWorksWithCompleteProfile() {
        givenScoreInputs(9L, completeProfile(), 1, 1, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getProfileCompletenessScore()).isEqualTo(100);
        assertThat(score.getProfileCompletenessContribution()).isEqualTo(20);
    }

    @Test
    void finalCredibilityScoreIsClampedTo100() {
        givenScoreInputs(9L, completeProfile(), 9, 4, 4, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getFinalCredibilityScore()).isEqualTo(100);
        assertThat(score.getTier()).isEqualTo(LevelPlayTier.ELITE);
    }

    @Test
    void tierMappingWorksForBronzeSilverGoldAndElite() {
        LevelPlayScore bronze = recalculate(1L, 0, 0, 0);
        LevelPlayScore silver = recalculate(2L, 1, 0, 0);
        LevelPlayScore gold = recalculate(3L, 3, 0, 0);
        LevelPlayScore elite = recalculate(4L, 6, 3, 3);

        assertThat(bronze.getTier()).isEqualTo(LevelPlayTier.BRONZE);
        assertThat(silver.getTier()).isEqualTo(LevelPlayTier.SILVER);
        assertThat(gold.getTier()).isEqualTo(LevelPlayTier.GOLD);
        assertThat(elite.getTier()).isEqualTo(LevelPlayTier.ELITE);
    }

    @Test
    void recalculationUpdatesExistingLevelPlayScore() {
        LevelPlayScore existing = LevelPlayScore.createPlaceholder(9L);
        givenScoreInputs(9L, completeProfile(), 2, 1, 1, Optional.of(existing));

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score).isSameAs(existing);
        assertThat(score.getFinalCredibilityScore()).isEqualTo(75);
        verify(levelPlayScoreRepository).save(same(existing));
        verify(notificationService).notifyLevelPlayScoreChanged(1L, 9L, 0, 75,
                LevelPlayTier.BRONZE, LevelPlayTier.ELITE);
    }

    @Test
    void recalculationCreatesLevelPlayScoreWhenMissing() {
        givenScoreInputs(9L, completeProfile(), 1, 1, 1, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthlete(9L);

        assertThat(score.getAthleteProfileId()).isEqualTo(9L);
        assertThat(score.getCalculatedAt()).isNotNull();
        verify(levelPlayScoreRepository).save(any(LevelPlayScore.class));
        verify(notificationService, never()).notifyLevelPlayScoreChanged(any(), any(), any(), any(), any(), any());
    }

    @Test
    void recalculationWithNoScoreChangeDoesNotCreateDuplicateNotification() {
        LevelPlayScore existing = LevelPlayScore.createPlaceholder(9L);
        existing.updateMvpScore(1, 1, 1, 100);
        givenScoreInputs(9L, completeProfile(), 1, 1, 1, Optional.of(existing));

        levelPlayScoreService.recalculateForAthlete(9L);

        verify(notificationService, never()).notifyLevelPlayScoreChanged(any(), any(), any(), any(), any(), any());
    }

    @Test
    void scoreExplanationMatchesCalculatedValues() {
        givenScoreInputs(9L, completeProfile(), 2, 2, 1, Optional.empty());

        LevelPlayScoreExplanationResponse explanation = levelPlayScoreService.explainScore(9L);

        assertThat(explanation.verifiedEvidenceCount()).isEqualTo(2);
        assertThat(explanation.verifiedEvidenceCountScore()).isEqualTo(35);
        assertThat(explanation.achievementScore()).isEqualTo(15);
        assertThat(explanation.coachVerificationScore()).isEqualTo(10);
        assertThat(explanation.finalCredibilityScore()).isEqualTo(80);
        assertThat(explanation.tier()).isEqualTo(LevelPlayTier.ELITE);
        assertThat(explanation.explanationText()).contains("Popularity");
    }

    @Test
    void adminActionLogIsCreatedWhenOneLevelPlayScoreIsRecalculatedByAdmin() {
        givenScoreInputs(9L, completeProfile(), 1, 0, 0, Optional.empty());

        LevelPlayScore score = levelPlayScoreService.recalculateForAthleteAsAdmin(9L, 99L);

        assertThat(score.getFinalCredibilityScore()).isEqualTo(38);
        verify(adminActionLogService).log(eq(99L), eq(AdminActionType.LEVELPLAY_RECALCULATED),
                eq(AdminTargetType.LEVELPLAY_SCORE), any(), any(), any());
    }

    @Test
    void adminActionLogIsCreatedWhenAllLevelPlayScoresAreRecalculatedByAdmin() {
        AthleteProfile profile = completeProfile();
        ReflectionTestUtils.setField(profile, "id", 1L);
        when(athleteProfileRepository.findAll()).thenReturn(List.of(profile));
        givenScoreInputs(1L, profile, 1, 0, 0, Optional.empty());

        List<LevelPlayScore> scores = levelPlayScoreService.recalculateAllScoresAsAdmin(99L);

        assertThat(scores).hasSize(1);
        verify(adminActionLogService).log(eq(99L), eq(AdminActionType.LEVELPLAY_RECALCULATE_ALL),
                eq(AdminTargetType.SYSTEM), eq(0L), any(), any());
    }

    private LevelPlayScore recalculate(Long athleteProfileId, int evidenceCount, int achievementCount,
                                       int coachVerificationCount) {
        givenScoreInputs(athleteProfileId, completeProfile(), evidenceCount, achievementCount,
                coachVerificationCount, Optional.empty());
        return levelPlayScoreService.recalculateForAthlete(athleteProfileId);
    }

    private void givenScoreInputs(Long athleteProfileId, AthleteProfile profile, long verifiedEvidenceCount,
                                  long achievementCount, long coachVerificationCount,
                                  Optional<LevelPlayScore> existingScore) {
        when(athleteProfileRepository.findById(athleteProfileId)).thenReturn(Optional.of(profile));
        when(levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId)).thenReturn(existingScore);
        when(evidenceUploadRepository.countByAthleteProfileIdAndVerificationStatus(athleteProfileId,
                VerificationStatus.VERIFIED)).thenReturn(verifiedEvidenceCount);
        when(evidenceUploadRepository.countByAthleteProfileId(athleteProfileId)).thenReturn(verifiedEvidenceCount);
        when(achievementRepository.countByAthleteProfileId(athleteProfileId)).thenReturn(achievementCount);
        when(verificationRequestRepository.countByAthleteProfileIdAndStatus(athleteProfileId,
                VerificationStatus.VERIFIED)).thenReturn(coachVerificationCount);
        when(userRepository.findById(profile.getUserId())).thenReturn(Optional.of(user("Athlete Name")));
        when(levelPlayScoreRepository.save(any(LevelPlayScore.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AthleteProfile completeProfile() {
        return AthleteProfile.create(1L, "Football", "Striker", 18, "Male",
                "Cape Town", "CPUT FC", "Bio");
    }

    private User user(String displayName) {
        User user = User.create(displayName, displayName.toLowerCase().replace(" ", ".") + "@example.com",
                null, "hashed", UserRole.ATHLETE);
        user.activate();
        return user;
    }
}
