package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.OrganisationService;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;
import za.co.ultronsport.web.dto.OrganisationResponse;

@RestController
@RequestMapping("/api/v1/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse create(@Valid @RequestBody CreateOrganisationRequest request) {
        return OrganisationResponse.from(organisationService.create(request));
    }

    @GetMapping("/{id}")
    public OrganisationResponse getById(@PathVariable Long id) {
        return OrganisationResponse.from(organisationService.getById(id));
    }
}
