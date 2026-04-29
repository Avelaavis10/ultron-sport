package za.co.ultronsport.service.impl;

import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.OrganisationService;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationServiceImpl(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @Override
    public Organisation create(CreateOrganisationRequest request) {
        return organisationRepository.save(Organisation.create(request.name(), request.type(), request.location(),
                request.primaryAdminUserId()));
    }

    @Override
    public Organisation getById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found: " + id));
    }
}
