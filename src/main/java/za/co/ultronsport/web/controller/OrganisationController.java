package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.service.OrganisationService;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;
import za.co.ultronsport.web.dto.OrganisationResponse;
import za.co.ultronsport.web.dto.PageResponse;
import za.co.ultronsport.web.dto.UpdateOrganisationRequest;

@RestController
@RequestMapping({"/api/organisations", "/api/v1/organisations"})
public class OrganisationController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "updatedAt", "name", "type", "location",
            "verificationStatus");

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationResponse create(@Valid @RequestBody CreateOrganisationRequest request,
                                       Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return OrganisationResponse.from(organisationService.create(currentUser.getId(), request));
    }

    @GetMapping("/{id}")
    public OrganisationResponse getById(@PathVariable Long id) {
        return OrganisationResponse.from(organisationService.getById(id));
    }

    @GetMapping
    public PageResponse<OrganisationResponse> search(@RequestParam(required = false) String name,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(required = false) String location,
                                                     @RequestParam(required = false)
                                                     VerificationStatus verificationStatus,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "updatedAt") String sortBy,
                                                     @RequestParam(defaultValue = "DESC")
                                                     Sort.Direction sortDirection) {
        validatePage(size, sortBy);
        Page<Organisation> organisations = organisationService.search(name, type, location, verificationStatus,
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
        List<OrganisationResponse> content = organisations.getContent().stream()
                .map(OrganisationResponse::from)
                .toList();
        return PageResponse.from(organisations, content, sortBy, sortDirection);
    }

    @PatchMapping("/{id}")
    public OrganisationResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateOrganisationRequest request) {
        return OrganisationResponse.from(organisationService.update(id, request));
    }

    private void validatePage(int size, String sortBy) {
        if (size < 1 || size > 50) {
            throw new InvalidStateException("Page size must be between 1 and 50.");
        }
        if (!SORT_FIELDS.contains(sortBy)) {
            throw new InvalidStateException("Invalid sort field: " + sortBy);
        }
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
