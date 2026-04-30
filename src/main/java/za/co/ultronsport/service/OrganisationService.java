package za.co.ultronsport.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;
import za.co.ultronsport.web.dto.UpdateOrganisationRequest;

public interface OrganisationService {
    Organisation create(Long currentUserId, CreateOrganisationRequest request);

    Organisation getById(Long id);

    Page<Organisation> search(String name, String type, String location, VerificationStatus verificationStatus,
                              Pageable pageable);

    Organisation update(Long organisationId, UpdateOrganisationRequest request);
}
