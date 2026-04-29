package za.co.ultronsport.service;

import za.co.ultronsport.domain.LevelPlayScore;

public interface LevelPlayScoreService {
    LevelPlayScore getOrCreateForAthlete(Long athleteProfileId);

    LevelPlayScore refreshPlaceholderScore(Long athleteProfileId);
}
