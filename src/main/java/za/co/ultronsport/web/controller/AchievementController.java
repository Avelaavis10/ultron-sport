package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.web.dto.AchievementResponse;
import za.co.ultronsport.web.dto.CreateAchievementRequest;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AchievementResponse create(@Valid @RequestBody CreateAchievementRequest request) {
        return AchievementResponse.from(achievementService.create(request));
    }

    @GetMapping("/athlete/{athleteProfileId}")
    public List<AchievementResponse> listForAthlete(@PathVariable Long athleteProfileId) {
        return achievementService.listForAthlete(athleteProfileId).stream()
                .map(AchievementResponse::from)
                .toList();
    }
}
