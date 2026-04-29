package za.co.ultronsport.web.dto;

import java.time.LocalDate;
import za.co.ultronsport.domain.Achievement;

public record AchievementSummaryResponse(
        Long id,
        String title,
        boolean verified,
        LocalDate achievedAt
) {
    public static AchievementSummaryResponse from(Achievement achievement) {
        return new AchievementSummaryResponse(achievement.getId(), achievement.getTitle(), achievement.isVerified(),
                achievement.getAchievedAt());
    }
}
