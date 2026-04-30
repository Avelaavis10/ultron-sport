package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.CreateAchievementRequest;
import za.co.ultronsport.web.dto.UpdateAchievementRequest;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final LevelPlayScoreService levelPlayScoreService;
    private final NotificationService notificationService;

    public AchievementServiceImpl(AchievementRepository achievementRepository,
                                  AthleteProfileRepository athleteProfileRepository,
                                  LevelPlayScoreService levelPlayScoreService,
                                  NotificationService notificationService) {
        this.achievementRepository = achievementRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.levelPlayScoreService = levelPlayScoreService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Achievement create(Long currentUserId, CreateAchievementRequest request) {
        AthleteProfile profile = getProfile(request.athleteProfileId());
        assertProfileOwner(currentUserId, profile);
        Achievement achievement = Achievement.create(request.athleteProfileId(), request.title(),
                request.description(), request.achievedAt());
        Achievement saved = achievementRepository.save(achievement);
        levelPlayScoreService.recalculateForAthlete(saved.getAthleteProfileId());
        notificationService.notifyAchievementCreated(profile.getUserId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> listMyAchievements(Long currentUserId) {
        AthleteProfile profile = athleteProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found for current user."));
        return achievementRepository.findByAthleteProfileIdOrderByAchievedAtDescCreatedAtDesc(profile.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> listForAthlete(Long currentUserId, UserRole currentUserRole, Long athleteProfileId) {
        AthleteProfile profile = getProfile(athleteProfileId);
        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.COACH
                || (currentUserRole == UserRole.ATHLETE && profile.getUserId().equals(currentUserId))) {
            return achievementRepository.findByAthleteProfileIdOrderByAchievedAtDescCreatedAtDesc(athleteProfileId);
        }
        throw new AccessDeniedException("You are not allowed to view these achievements.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> listForAthlete(Long athleteProfileId) {
        return achievementRepository.findByAthleteProfileIdOrderByAchievedAtDescCreatedAtDesc(athleteProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Achievement> listAll(Pageable pageable) {
        return achievementRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Achievement update(Long currentUserId, Long achievementId, UpdateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));
        AthleteProfile profile = getProfile(achievement.getAthleteProfileId());
        assertProfileOwner(currentUserId, profile);
        try {
            achievement.updateDetails(request.title(), request.description(), request.achievedAt());
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
        Achievement saved = achievementRepository.save(achievement);
        levelPlayScoreService.recalculateForAthlete(saved.getAthleteProfileId());
        return saved;
    }

    private AthleteProfile getProfile(Long athleteProfileId) {
        return athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
    }

    private void assertProfileOwner(Long currentUserId, AthleteProfile profile) {
        if (!profile.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only manage achievements for your own athlete profile.");
        }
    }
}
