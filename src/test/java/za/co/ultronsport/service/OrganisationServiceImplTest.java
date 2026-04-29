package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.impl.OrganisationServiceImpl;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceImplTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private OrganisationServiceImpl organisationService;

    @Test
    void createBuildsOrganisation() {
        CreateOrganisationRequest request = new CreateOrganisationRequest("Cape School", "School", "Cape Town", 1L);
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Organisation organisation = organisationService.create(request);

        assertThat(organisation.getName()).isEqualTo("Cape School");
    }
}
