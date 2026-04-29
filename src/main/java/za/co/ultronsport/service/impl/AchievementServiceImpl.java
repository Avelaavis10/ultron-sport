package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.web.dto.CreateAchievementRequest;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;

    public AchievementServiceImpl(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public Achievement create(CreateAchievementRequest request) {
        Achievement achievement = Achievement.create(request.athleteProfileId(), request.title(),
                request.description(), request.achievedAt());
        return achievementRepository.save(achievement);
    }

    @Override
    public List<Achievement> listForAthlete(Long athleteProfileId) {
        return achievementRepository.findByAthleteProfileId(athleteProfileId);
    }
}
