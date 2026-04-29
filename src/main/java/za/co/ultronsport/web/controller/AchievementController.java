package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.web.dto.AchievementResponse;
import za.co.ultronsport.web.dto.CreateAchievementRequest;
import za.co.ultronsport.web.dto.PageResponse;
import za.co.ultronsport.web.dto.UpdateAchievementRequest;

@RestController
@RequestMapping({"/api/achievements", "/api/v1/achievements"})
public class AchievementController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "achievedAt", "title");

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AchievementResponse create(@Valid @RequestBody CreateAchievementRequest request,
                                      Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AchievementResponse.from(achievementService.create(currentUser.getId(), request));
    }

    @GetMapping("/my")
    public List<AchievementResponse> myAchievements(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return achievementService.listMyAchievements(currentUser.getId()).stream()
                .map(AchievementResponse::from)
                .toList();
    }

    @PatchMapping("/{achievementId}")
    public AchievementResponse update(@PathVariable Long achievementId,
                                      @Valid @RequestBody UpdateAchievementRequest request,
                                      Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AchievementResponse.from(achievementService.update(currentUser.getId(), achievementId, request));
    }

    @GetMapping
    public PageResponse<AchievementResponse> listAll(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                                     @RequestParam(defaultValue = "DESC")
                                                     Sort.Direction sortDirection) {
        validatePage(size, sortBy);
        Page<Achievement> achievements = achievementService.listAll(PageRequest.of(page, size,
                Sort.by(sortDirection, sortBy)));
        List<AchievementResponse> content = achievements.getContent().stream()
                .map(AchievementResponse::from)
                .toList();
        return PageResponse.from(achievements, content, sortBy, sortDirection);
    }

    @GetMapping("/athlete/{athleteProfileId}")
    public List<AchievementResponse> listForAthlete(@PathVariable Long athleteProfileId,
                                                    Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return achievementService.listForAthlete(currentUser.getId(), currentUser.getRole(), athleteProfileId).stream()
                .map(AchievementResponse::from)
                .toList();
    }

    private void validatePage(int size, String sortBy) {
        if (size > 50) {
            throw new InvalidStateException("Page size must not exceed 50.");
        }
        if (!SORT_FIELDS.contains(sortBy)) {
            throw new InvalidStateException("Invalid sort field: " + sortBy);
        }
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
