package za.co.ultronsport.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.web.dto.CreateAchievementRequest;
import za.co.ultronsport.web.dto.UpdateAchievementRequest;

public interface AchievementService {
    Achievement create(Long currentUserId, CreateAchievementRequest request);

    List<Achievement> listMyAchievements(Long currentUserId);

    List<Achievement> listForAthlete(Long currentUserId, UserRole currentUserRole, Long athleteProfileId);

    List<Achievement> listForAthlete(Long athleteProfileId);

    Page<Achievement> listAll(Pageable pageable);

    Achievement update(Long currentUserId, Long achievementId, UpdateAchievementRequest request);
}
