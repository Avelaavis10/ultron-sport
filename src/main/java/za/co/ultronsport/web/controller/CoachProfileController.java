package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.service.CoachProfileService;
import za.co.ultronsport.web.dto.CoachProfileResponse;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;
import za.co.ultronsport.web.dto.UpdateCoachProfileRequest;

@RestController
@RequestMapping({"/api/coach-profiles", "/api/v1/coach-profiles"})
public class CoachProfileController {

    private final CoachProfileService coachProfileService;

    public CoachProfileController(CoachProfileService coachProfileService) {
        this.coachProfileService = coachProfileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoachProfileResponse create(@Valid @RequestBody CreateCoachProfileRequest request,
                                       Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return CoachProfileResponse.from(coachProfileService.create(currentUser.getId(), request));
    }

    @GetMapping("/me")
    public CoachProfileResponse me(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return CoachProfileResponse.from(coachProfileService.getMyProfile(currentUser.getId()));
    }

    @PatchMapping("/me")
    public CoachProfileResponse updateMe(@Valid @RequestBody UpdateCoachProfileRequest request,
                                         Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return CoachProfileResponse.from(coachProfileService.updateMyProfile(currentUser.getId(), request));
    }

    @GetMapping("/{id}")
    public CoachProfileResponse getById(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return CoachProfileResponse.from(coachProfileService.getById(currentUser.getId(), currentUser.getRole(), id));
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
