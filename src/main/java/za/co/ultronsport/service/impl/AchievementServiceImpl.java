package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.web.dto.CreateAchievementRequest;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final LevelPlayScoreService levelPlayScoreService;

    public AchievementServiceImpl(AchievementRepository achievementRepository,
                                  LevelPlayScoreService levelPlayScoreService) {
        this.achievementRepository = achievementRepository;
        this.levelPlayScoreService = levelPlayScoreService;
    }

    @Override
    public Achievement create(CreateAchievementRequest request) {
        Achievement achievement = Achievement.create(request.athleteProfileId(), request.title(),
                request.description(), request.achievedAt());
        Achievement saved = achievementRepository.save(achievement);
        levelPlayScoreService.recalculateForAthlete(saved.getAthleteProfileId());
        return saved;
    }

    @Override
    public List<Achievement> listForAthlete(Long athleteProfileId) {
        return achievementRepository.findByAthleteProfileId(athleteProfileId);
    }
}
