package za.co.ultronsport.service.impl;

import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.service.LevelPlayScoreService;

@Service
public class LevelPlayScoreServiceImpl implements LevelPlayScoreService {

    private final LevelPlayScoreRepository levelPlayScoreRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final EvidenceUploadRepository evidenceUploadRepository;
    private final AchievementRepository achievementRepository;

    public LevelPlayScoreServiceImpl(LevelPlayScoreRepository levelPlayScoreRepository,
                                     AthleteProfileRepository athleteProfileRepository,
                                     EvidenceUploadRepository evidenceUploadRepository,
                                     AchievementRepository achievementRepository) {
        this.levelPlayScoreRepository = levelPlayScoreRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.evidenceUploadRepository = evidenceUploadRepository;
        this.achievementRepository = achievementRepository;
    }

    @Override
    public LevelPlayScore getOrCreateForAthlete(Long athleteProfileId) {
        return levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId)
                .orElseGet(() -> levelPlayScoreRepository.save(LevelPlayScore.createPlaceholder(athleteProfileId)));
    }

    @Override
    public LevelPlayScore refreshPlaceholderScore(Long athleteProfileId) {
        AthleteProfile profile = athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        LevelPlayScore score = getOrCreateForAthlete(athleteProfileId);
        long verifiedEvidenceCount = evidenceUploadRepository.countByAthleteProfileIdAndVerificationStatus(
                athleteProfileId, VerificationStatus.VERIFIED);
        long achievementCount = achievementRepository.countByAthleteProfileId(athleteProfileId);
        score.updatePlaceholderMetrics((int) verifiedEvidenceCount, (int) verifiedEvidenceCount,
                (int) achievementCount, profile.getProfileCompletenessScore(), 0);
        return levelPlayScoreRepository.save(score);
    }
}
