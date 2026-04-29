package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.web.dto.CreateAchievementRequest;

public interface AchievementService {
    Achievement create(CreateAchievementRequest request);

    List<Achievement> listForAthlete(Long athleteProfileId);
}
