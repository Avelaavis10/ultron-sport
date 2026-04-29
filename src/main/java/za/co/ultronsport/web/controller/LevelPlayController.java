package za.co.ultronsport.web.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.web.dto.LevelPlayScoreExplanationResponse;
import za.co.ultronsport.web.dto.LevelPlayScoreResponse;

@RestController
@RequestMapping("/api/levelplay")
public class LevelPlayController {

    private final LevelPlayScoreService levelPlayScoreService;

    public LevelPlayController(LevelPlayScoreService levelPlayScoreService) {
        this.levelPlayScoreService = levelPlayScoreService;
    }

    @GetMapping("/athletes/{athleteProfileId}")
    public LevelPlayScoreResponse getAthleteScore(@PathVariable Long athleteProfileId) {
        return LevelPlayScoreResponse.from(levelPlayScoreService.getScoreForAthlete(athleteProfileId));
    }

    @GetMapping("/me")
    public LevelPlayScoreResponse getMyScore(Authentication authentication) {
        SecurityUser currentUser = (SecurityUser) authentication.getPrincipal();
        return LevelPlayScoreResponse.from(levelPlayScoreService.getMyScore(currentUser.getId()));
    }

    @PostMapping("/athletes/{athleteProfileId}/recalculate")
    public LevelPlayScoreResponse recalculateAthleteScore(@PathVariable Long athleteProfileId,
                                                          Authentication authentication) {
        SecurityUser currentUser = (SecurityUser) authentication.getPrincipal();
        return LevelPlayScoreResponse.from(levelPlayScoreService.recalculateForAthleteAsAdmin(athleteProfileId,
                currentUser.getId()));
    }

    @PostMapping("/recalculate-all")
    public List<LevelPlayScoreResponse> recalculateAllScores(Authentication authentication) {
        SecurityUser currentUser = (SecurityUser) authentication.getPrincipal();
        return levelPlayScoreService.recalculateAllScoresAsAdmin(currentUser.getId()).stream()
                .map(LevelPlayScoreResponse::from)
                .toList();
    }

    @GetMapping("/athletes/{athleteProfileId}/explain")
    public LevelPlayScoreExplanationResponse explainAthleteScore(@PathVariable Long athleteProfileId) {
        return levelPlayScoreService.explainScore(athleteProfileId);
    }
}
