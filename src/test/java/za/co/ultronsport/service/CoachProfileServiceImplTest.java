package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.service.impl.CoachProfileServiceImpl;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;

@ExtendWith(MockitoExtension.class)
class CoachProfileServiceImplTest {

    @Mock
    private CoachProfileRepository coachProfileRepository;

    @InjectMocks
    private CoachProfileServiceImpl coachProfileService;

    @Test
    void createBuildsCoachProfile() {
        CreateCoachProfileRequest request = new CreateCoachProfileRequest(1L, "SAFA-123", "CPUT FC", "Football");
        when(coachProfileRepository.save(any(CoachProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoachProfile profile = coachProfileService.create(request);

        assertThat(profile.getCertificationReference()).isEqualTo("SAFA-123");
    }
}
