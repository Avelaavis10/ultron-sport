package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.impl.OrganisationServiceImpl;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;
import za.co.ultronsport.web.dto.UpdateOrganisationRequest;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceImplTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private OrganisationServiceImpl organisationService;

    @Test
    void createBuildsOrganisation() {
        CreateOrganisationRequest request = new CreateOrganisationRequest("Cape School", "School", "Cape Town",
                "school@example.com", null);
        when(organisationRepository.findByNameIgnoreCaseAndLocationIgnoreCase("Cape School", "Cape Town"))
                .thenReturn(Optional.empty());
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Organisation organisation = organisationService.create(1L, request);

        assertThat(organisation.getName()).isEqualTo("Cape School");
        assertThat(organisation.getContactEmail()).isEqualTo("school@example.com");
        assertThat(organisation.getPrimaryAdminUserId()).isEqualTo(1L);
    }

    @Test
    void createRejectsMissingNameTypeOrLocation() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(" ", "School", "Cape Town", null, null);

        assertThatThrownBy(() -> organisationService.create(1L, request))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Organisation name is required.");
    }

    @Test
    void adminCanUpdateOrganisation() {
        Organisation organisation = Organisation.create("Cape School", "School", "Cape Town", null, 1L);
        when(organisationRepository.findById(3L)).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(organisation)).thenReturn(organisation);

        Organisation updated = organisationService.update(3L,
                new UpdateOrganisationRequest("Cape Academy", "Academy", "Bellville",
                        "academy@example.com", VerificationStatus.VERIFIED));

        assertThat(updated.getName()).isEqualTo("Cape Academy");
        assertThat(updated.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        verify(organisationRepository).save(organisation);
    }
}
