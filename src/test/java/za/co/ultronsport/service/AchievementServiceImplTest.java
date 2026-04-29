package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.service.impl.AchievementServiceImpl;
import za.co.ultronsport.web.dto.CreateAchievementRequest;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Test
    void createBuildsAchievement() {
        CreateAchievementRequest request = new CreateAchievementRequest(1L, "Top Scorer", "League top scorer",
                LocalDate.now());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Achievement achievement = achievementService.create(request);

        assertThat(achievement.getTitle()).isEqualTo("Top Scorer");
    }
}
