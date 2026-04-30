package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.service.AchievementService;
import za.co.ultronsport.service.AthleteProfileService;
import za.co.ultronsport.web.dto.AchievementResponse;
import za.co.ultronsport.web.dto.AthleteProfileResponse;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;
import za.co.ultronsport.web.dto.LinkAthleteOrganisationRequest;
import za.co.ultronsport.web.dto.PageResponse;
import za.co.ultronsport.web.dto.UpdateAthleteProfileRequest;

@RestController
@RequestMapping({"/api/athlete-profiles", "/api/v1/athlete-profiles"})
public class AthleteProfileController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "sport", "position",
            "location", "profileCompletenessScore");

    private final AthleteProfileService athleteProfileService;
    private final AchievementService achievementService;

    public AthleteProfileController(AthleteProfileService athleteProfileService,
                                    AchievementService achievementService) {
        this.athleteProfileService = athleteProfileService;
        this.achievementService = achievementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteProfileResponse create(@Valid @RequestBody CreateAthleteProfileRequest request,
                                         Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AthleteProfileResponse.from(athleteProfileService.create(currentUser.getId(), request));
    }

    @GetMapping("/me")
    public AthleteProfileResponse me(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AthleteProfileResponse.from(athleteProfileService.getMyProfile(currentUser.getId()));
    }

    @PatchMapping("/me")
    public AthleteProfileResponse updateMe(@Valid @RequestBody UpdateAthleteProfileRequest request,
                                           Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AthleteProfileResponse.from(athleteProfileService.updateMyProfile(currentUser.getId(), request));
    }

    @PatchMapping("/me/organisation")
    public AthleteProfileResponse linkOrganisation(@Valid @RequestBody LinkAthleteOrganisationRequest request,
                                                   Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AthleteProfileResponse.from(athleteProfileService.linkOrganisation(currentUser.getId(), request));
    }

    @GetMapping("/{id}")
    public AthleteProfileResponse getById(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return AthleteProfileResponse.from(athleteProfileService.getById(currentUser.getId(),
                currentUser.getRole(), id));
    }

    @GetMapping("/{athleteProfileId}/achievements")
    public List<AchievementResponse> achievements(@PathVariable Long athleteProfileId,
                                                  Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return achievementService.listForAthlete(currentUser.getId(), currentUser.getRole(), athleteProfileId)
                .stream()
                .map(AchievementResponse::from)
                .toList();
    }

    @GetMapping
    public PageResponse<AthleteProfileResponse> search(@RequestParam(required = false) String sport,
                                                       @RequestParam(required = false) String location,
                                                       @RequestParam(required = false) String position,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(defaultValue = "updatedAt") String sortBy,
                                                       @RequestParam(defaultValue = "DESC")
                                                       Sort.Direction sortDirection) {
        validatePage(size, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<AthleteProfile> profiles;
        if (hasSearchFilters(sport, location, position)) {
            List<AthleteProfile> results = athleteProfileService.search(sport, location, position);
            profiles = new PageImpl<>(results, pageRequest, results.size());
        } else {
            profiles = athleteProfileService.listAll(pageRequest);
        }
        List<AthleteProfileResponse> content = profiles.getContent().stream()
                .map(AthleteProfileResponse::from)
                .toList();
        return PageResponse.from(profiles, content, sortBy, sortDirection);
    }

    private void validatePage(int size, String sortBy) {
        if (size > 50) {
            throw new InvalidStateException("Page size must not exceed 50.");
        }
        if (!SORT_FIELDS.contains(sortBy)) {
            throw new InvalidStateException("Invalid sort field: " + sortBy);
        }
    }

    private boolean hasSearchFilters(String sport, String location, String position) {
        return hasText(sport) || hasText(location) || hasText(position);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
