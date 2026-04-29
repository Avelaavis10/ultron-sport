package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.web.dto.LevelPlayScoreExplanationResponse;

public interface LevelPlayScoreService {
    LevelPlayScore calculateForAthlete(Long athleteProfileId);

    LevelPlayScore recalculateForAthlete(Long athleteProfileId);

    LevelPlayScore getScoreForAthlete(Long athleteProfileId);

    LevelPlayScore getMyScore(Long currentUserId);

    List<LevelPlayScore> recalculateAllScores();

    LevelPlayScoreExplanationResponse explainScore(Long athleteProfileId);

    LevelPlayScore getOrCreateForAthlete(Long athleteProfileId);

    LevelPlayScore refreshPlaceholderScore(Long athleteProfileId);
}
