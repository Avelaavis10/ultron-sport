package za.co.ultronsport.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.LevelPlayScoreExplanationResponse;

@Service
public class LevelPlayScoreServiceImpl implements LevelPlayScoreService {

    private final LevelPlayScoreRepository levelPlayScoreRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final EvidenceUploadRepository evidenceUploadRepository;
    private final AchievementRepository achievementRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;
    private final AdminActionLogService adminActionLogService;
    private final NotificationService notificationService;

    public LevelPlayScoreServiceImpl(LevelPlayScoreRepository levelPlayScoreRepository,
                                     AthleteProfileRepository athleteProfileRepository,
                                     EvidenceUploadRepository evidenceUploadRepository,
                                     AchievementRepository achievementRepository,
                                     VerificationRequestRepository verificationRequestRepository,
                                     UserRepository userRepository,
                                     AdminActionLogService adminActionLogService,
                                     NotificationService notificationService) {
        this.levelPlayScoreRepository = levelPlayScoreRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.evidenceUploadRepository = evidenceUploadRepository;
        this.achievementRepository = achievementRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.userRepository = userRepository;
        this.adminActionLogService = adminActionLogService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public LevelPlayScore calculateForAthlete(Long athleteProfileId) {
        return recalculateForAthlete(athleteProfileId);
    }

    @Override
    @Transactional
    public LevelPlayScore recalculateForAthlete(Long athleteProfileId) {
        return recalculateForAthlete(athleteProfileId, true);
    }

    private LevelPlayScore recalculateForAthlete(Long athleteProfileId, boolean notifyOnChange) {
        AthleteProfile profile = athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        Optional<LevelPlayScore> existingScore = levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId);
        Integer oldFinalScore = existingScore.map(LevelPlayScore::getFinalCredibilityScore).orElse(null);
        LevelPlayTier oldTier = existingScore.map(LevelPlayScore::getTier).orElse(null);
        LevelPlayScore score = existingScore.orElseGet(() -> LevelPlayScore.createPlaceholder(athleteProfileId));
        int verifiedEvidenceCount = safeInt(evidenceUploadRepository.countByAthleteProfileIdAndVerificationStatus(
                athleteProfileId, VerificationStatus.VERIFIED));
        int achievementCount = safeInt(achievementRepository.countByAthleteProfileId(athleteProfileId));
        int evidenceItemCount = safeInt(evidenceUploadRepository.countByAthleteProfileId(athleteProfileId));
        int coachVerificationCount = safeInt(verificationRequestRepository.countByAthleteProfileIdAndStatus(
                athleteProfileId, VerificationStatus.VERIFIED));
        int profileCompletenessScore = calculateProfileCompletenessScore(profile, achievementCount, evidenceItemCount);
        profile.updateProfileCompletenessScore(profileCompletenessScore);
        athleteProfileRepository.save(profile);
        score.updateMvpScore(verifiedEvidenceCount, coachVerificationCount, achievementCount, profileCompletenessScore);
        LevelPlayScore saved = levelPlayScoreRepository.save(score);
        if (notifyOnChange && existingScore.isPresent() && scoreChanged(oldFinalScore, oldTier, saved)) {
            notificationService.notifyLevelPlayScoreChanged(profile.getUserId(), athleteProfileId, oldFinalScore,
                    saved.getFinalCredibilityScore(), oldTier, saved.getTier());
        }
        return saved;
    }

    @Override
    @Transactional
    public LevelPlayScore recalculateForAthleteAsAdmin(Long athleteProfileId, Long adminUserId) {
        LevelPlayScore score = recalculateForAthlete(athleteProfileId);
        adminActionLogService.log(adminUserId, AdminActionType.LEVELPLAY_RECALCULATED,
                AdminTargetType.LEVELPLAY_SCORE, score.getId(), null,
                "athleteProfileId=" + athleteProfileId + ", finalCredibilityScore="
                        + score.getFinalCredibilityScore());
        return score;
    }

    @Override
    @Transactional
    public LevelPlayScore getScoreForAthlete(Long athleteProfileId) {
        athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        return levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId)
                .orElseGet(() -> recalculateForAthlete(athleteProfileId));
    }

    @Override
    @Transactional
    public LevelPlayScore getMyScore(Long currentUserId) {
        AthleteProfile profile = athleteProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found for current user."));
        return recalculateForAthlete(profile.getId());
    }

    @Override
    @Transactional
    public List<LevelPlayScore> recalculateAllScores() {
        return athleteProfileRepository.findAll().stream()
                .map(profile -> recalculateForAthlete(profile.getId(), false))
                .toList();
    }

    @Override
    @Transactional
    public List<LevelPlayScore> recalculateAllScoresAsAdmin(Long adminUserId) {
        List<LevelPlayScore> scores = recalculateAllScores();
        adminActionLogService.log(adminUserId, AdminActionType.LEVELPLAY_RECALCULATE_ALL,
                AdminTargetType.SYSTEM, 0L, null, "recalculatedAthleteCount=" + scores.size());
        return scores;
    }

    @Override
    @Transactional
    public LevelPlayScoreExplanationResponse explainScore(Long athleteProfileId) {
        LevelPlayScore score = recalculateForAthlete(athleteProfileId);
        return LevelPlayScoreExplanationResponse.from(score);
    }

    @Override
    @Transactional
    public LevelPlayScore getOrCreateForAthlete(Long athleteProfileId) {
        return levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId)
                .orElseGet(() -> levelPlayScoreRepository.save(LevelPlayScore.createPlaceholder(athleteProfileId)));
    }

    @Override
    @Transactional
    public LevelPlayScore refreshPlaceholderScore(Long athleteProfileId) {
        return recalculateForAthlete(athleteProfileId);
    }

    int calculateProfileCompletenessScore(AthleteProfile profile, int achievementCount, int evidenceItemCount) {
        int completed = 0;
        int total = 9;
        completed += hasDisplayName(profile.getUserId()) ? 1 : 0;
        completed += hasText(profile.getSport()) ? 1 : 0;
        completed += hasText(profile.getPosition()) ? 1 : 0;
        completed += hasText(profile.getLocation()) ? 1 : 0;
        completed += hasOrganisation(profile) ? 1 : 0;
        completed += hasText(profile.getBio()) ? 1 : 0;
        completed += profile.getAge() != null ? 1 : 0;
        completed += achievementCount > 0 ? 1 : 0;
        completed += evidenceItemCount > 0 ? 1 : 0;
        return Math.round((completed * 100f) / total);
    }

    private boolean hasDisplayName(Long userId) {
        if (userId == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(User::getDisplayName)
                .filter(this::hasText)
                .isPresent();
    }

    private boolean hasOrganisation(AthleteProfile profile) {
        return profile.getOrganisationId() != null || hasText(profile.getSchoolOrClub());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int safeInt(long value) {
        return Math.toIntExact(Math.max(0, value));
    }

    private boolean scoreChanged(Integer oldFinalScore, LevelPlayTier oldTier, LevelPlayScore saved) {
        return !Objects.equals(oldFinalScore, saved.getFinalCredibilityScore())
                || !Objects.equals(oldTier, saved.getTier());
    }
}
