package za.co.ultronsport.service;

import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;

public interface OrganisationService {
    Organisation create(CreateOrganisationRequest request);

    Organisation getById(Long id);
}
