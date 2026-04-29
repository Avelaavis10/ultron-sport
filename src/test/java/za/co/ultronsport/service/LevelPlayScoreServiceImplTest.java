package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.service.impl.LevelPlayScoreServiceImpl;

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

    @InjectMocks
    private LevelPlayScoreServiceImpl levelPlayScoreService;

    @Test
    void refreshPlaceholderScoreUsesVerifiedEvidenceAndAchievements() {
        AthleteProfile profile = AthleteProfile.create(1L, "Football", "Striker", 18, "Male",
                "Cape Town", "CPUT FC", "Bio");
        LevelPlayScore score = LevelPlayScore.createPlaceholder(9L);
        when(athleteProfileRepository.findById(9L)).thenReturn(Optional.of(profile));
        when(levelPlayScoreRepository.findByAthleteProfileId(9L)).thenReturn(Optional.of(score));
        when(evidenceUploadRepository.countByAthleteProfileIdAndVerificationStatus(9L, VerificationStatus.VERIFIED))
                .thenReturn(2L);
        when(achievementRepository.countByAthleteProfileId(9L)).thenReturn(1L);
        when(levelPlayScoreRepository.save(score)).thenReturn(score);

        LevelPlayScore refreshed = levelPlayScoreService.refreshPlaceholderScore(9L);

        assertThat(refreshed.getVerifiedEvidenceCount()).isEqualTo(2);
        assertThat(refreshed.getAchievementCount()).isEqualTo(1);
        assertThat(refreshed.getFinalCredibilityScore()).isGreaterThan(0);
    }
}
