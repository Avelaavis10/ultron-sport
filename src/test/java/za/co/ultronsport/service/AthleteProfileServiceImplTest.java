package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.service.impl.AthleteProfileServiceImpl;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;

@ExtendWith(MockitoExtension.class)
class AthleteProfileServiceImplTest {

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private LevelPlayScoreService levelPlayScoreService;

    @InjectMocks
    private AthleteProfileServiceImpl athleteProfileService;

    @Test
    void createBuildsAthleteProfile() {
        CreateAthleteProfileRequest request = new CreateAthleteProfileRequest(1L, "Football", "Striker",
                18, "Male", "Cape Town", "CPUT FC", "Fast finisher");
        when(athleteProfileRepository.save(any(AthleteProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile profile = athleteProfileService.create(request);

        assertThat(profile.getSport()).isEqualTo("Football");
        assertThat(profile.getProfileCompletenessScore()).isGreaterThan(80);
        verify(levelPlayScoreService).recalculateForAthlete(profile.getId());
    }

    @Test
    void searchDelegatesToRepositorySpecification() {
        when(athleteProfileRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThat(athleteProfileService.search("Football", "Cape Town", "Striker")).isEmpty();
    }
}
