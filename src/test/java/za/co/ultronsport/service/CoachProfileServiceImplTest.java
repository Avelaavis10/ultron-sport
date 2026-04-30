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
import org.springframework.security.access.AccessDeniedException;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.impl.CoachProfileServiceImpl;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;
import za.co.ultronsport.web.dto.UpdateCoachProfileRequest;

@ExtendWith(MockitoExtension.class)
class CoachProfileServiceImplTest {

    @Mock
    private CoachProfileRepository coachProfileRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CoachProfileServiceImpl coachProfileService;

    @Test
    void createBuildsCoachProfile() {
        Organisation organisation = Organisation.create("CPUT FC", "Club", "Cape Town", null, 1L);
        CreateCoachProfileRequest request = new CreateCoachProfileRequest("SAFA-123", 3L, null, "Football",
                "Qualified coach", 5);
        when(coachProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(organisationRepository.findById(3L)).thenReturn(Optional.of(organisation));
        when(coachProfileRepository.save(any(CoachProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoachProfile profile = coachProfileService.create(1L, request);

        assertThat(profile.getCertificationReference()).isEqualTo("SAFA-123");
        assertThat(profile.getOrganisationId()).isEqualTo(3L);
        assertThat(profile.getOrganisationName()).isEqualTo("CPUT FC");
        verify(notificationService).notifyCoachProfileUpdated(1L, null);
    }

    @Test
    void coachCannotCreateDuplicateProfile() {
        when(coachProfileRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> coachProfileService.create(1L,
                new CreateCoachProfileRequest("SAFA-123", null, "CPUT FC", "Football", null, 3)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Coach profile already exists for current user.");
    }

    @Test
    void coachCanUpdateOwnProfile() {
        CoachProfile profile = CoachProfile.create(1L, "OLD-1", null, "Old Club", "Football", null, 1);
        when(coachProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(coachProfileRepository.save(profile)).thenReturn(profile);

        CoachProfile updated = coachProfileService.updateMyProfile(1L,
                new UpdateCoachProfileRequest("SAFA-999", null, "New Club", "Rugby", "Updated", 7));

        assertThat(updated.getCertificationReference()).isEqualTo("SAFA-999");
        assertThat(updated.getYearsExperience()).isEqualTo(7);
        verify(notificationService).notifyCoachProfileUpdated(1L, null);
    }

    @Test
    void coachCannotViewAnotherCoachProfile() {
        CoachProfile profile = CoachProfile.create(2L, "SAFA-123", null, "CPUT FC", "Football", null, 2);
        when(coachProfileRepository.findById(4L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> coachProfileService.getById(1L, UserRole.COACH, 4L))
                .isInstanceOf(AccessDeniedException.class);
    }
}
