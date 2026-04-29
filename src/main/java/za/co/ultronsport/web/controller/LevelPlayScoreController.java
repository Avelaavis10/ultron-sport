package za.co.ultronsport.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.web.dto.LevelPlayScoreResponse;

@RestController
@RequestMapping("/api/v1/levelplay-scores")
public class LevelPlayScoreController {

    private final LevelPlayScoreService levelPlayScoreService;

    public LevelPlayScoreController(LevelPlayScoreService levelPlayScoreService) {
        this.levelPlayScoreService = levelPlayScoreService;
    }

    @GetMapping("/athlete/{athleteProfileId}")
    public LevelPlayScoreResponse getForAthlete(@PathVariable Long athleteProfileId) {
        return LevelPlayScoreResponse.from(levelPlayScoreService.getScoreForAthlete(athleteProfileId));
    }

    @PostMapping("/athlete/{athleteProfileId}/refresh")
    public LevelPlayScoreResponse refreshForAthlete(@PathVariable Long athleteProfileId) {
        return LevelPlayScoreResponse.from(levelPlayScoreService.recalculateForAthlete(athleteProfileId));
    }
}
