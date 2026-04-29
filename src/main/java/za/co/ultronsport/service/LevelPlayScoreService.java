package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.web.dto.LevelPlayScoreExplanationResponse;

public interface LevelPlayScoreService {
    LevelPlayScore calculateForAthlete(Long athleteProfileId);

    LevelPlayScore recalculateForAthlete(Long athleteProfileId);

    LevelPlayScore recalculateForAthleteAsAdmin(Long athleteProfileId, Long adminUserId);

    LevelPlayScore getScoreForAthlete(Long athleteProfileId);

    LevelPlayScore getMyScore(Long currentUserId);

    List<LevelPlayScore> recalculateAllScores();

    List<LevelPlayScore> recalculateAllScoresAsAdmin(Long adminUserId);

    LevelPlayScoreExplanationResponse explainScore(Long athleteProfileId);

    LevelPlayScore getOrCreateForAthlete(Long athleteProfileId);

    LevelPlayScore refreshPlaceholderScore(Long athleteProfileId);
}
