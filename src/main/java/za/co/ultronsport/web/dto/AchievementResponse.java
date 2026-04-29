package za.co.ultronsport.web.dto;

import java.time.LocalDate;
import za.co.ultronsport.domain.Achievement;

public record AchievementResponse(
        Long id,
        Long athleteProfileId,
        String title,
        String description,
        LocalDate achievedAt,
        boolean verified
) {
    public static AchievementResponse from(Achievement achievement) {
        return new AchievementResponse(achievement.getId(), achievement.getAthleteProfileId(),
                achievement.getTitle(), achievement.getDescription(), achievement.getAchievedAt(),
                achievement.isVerified());
    }
}
