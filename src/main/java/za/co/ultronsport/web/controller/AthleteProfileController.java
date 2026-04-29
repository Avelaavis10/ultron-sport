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
import za.co.ultronsport.service.AthleteProfileService;
import za.co.ultronsport.web.dto.AthleteProfileResponse;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;

@RestController
@RequestMapping("/api/v1/athlete-profiles")
public class AthleteProfileController {

    private final AthleteProfileService athleteProfileService;

    public AthleteProfileController(AthleteProfileService athleteProfileService) {
        this.athleteProfileService = athleteProfileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteProfileResponse create(@Valid @RequestBody CreateAthleteProfileRequest request) {
        return AthleteProfileResponse.from(athleteProfileService.create(request));
    }

    @GetMapping("/{id}")
    public AthleteProfileResponse getById(@PathVariable Long id) {
        return AthleteProfileResponse.from(athleteProfileService.getById(id));
    }

    @GetMapping
    public List<AthleteProfileResponse> search(String sport, String location, String position) {
        return athleteProfileService.search(sport, location, position).stream()
                .map(AthleteProfileResponse::from)
                .toList();
    }
}
