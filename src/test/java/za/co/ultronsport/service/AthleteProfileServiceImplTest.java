package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.ultronsport.common.error.DuplicateResourceException;
import org.springframework.data.jpa.domain.Specification;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.service.impl.AthleteProfileServiceImpl;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;
import za.co.ultronsport.web.dto.UpdateAthleteProfileRequest;

@ExtendWith(MockitoExtension.class)
class AthleteProfileServiceImplTest {

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private LevelPlayScoreService levelPlayScoreService;

    @InjectMocks
    private AthleteProfileServiceImpl athleteProfileService;

    @Test
    void athleteCanCreateOwnProfile() {
        CreateAthleteProfileRequest request = createRequest();
        when(athleteProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(athleteProfileRepository.save(any(AthleteProfile.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 11L));

        AthleteProfile profile = athleteProfileService.create(1L, request);

        assertThat(profile.getSport()).isEqualTo("Football");
        assertThat(profile.getProfileCompletenessScore()).isGreaterThan(80);
        verify(levelPlayScoreService).recalculateForAthlete(11L);
    }

    @Test
    void athleteCannotCreateDuplicateProfile() {
        when(athleteProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile(1L)));

        assertThatThrownBy(() -> athleteProfileService.create(1L, createRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Athlete profile already exists for current user.");
    }

    @Test
    void athleteCanUpdateOwnProfile() {
        AthleteProfile profile = profile(1L);
        ReflectionTestUtils.setField(profile, "id", 11L);
        when(athleteProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(athleteProfileRepository.save(profile)).thenReturn(profile);

        AthleteProfile updated = athleteProfileService.updateMyProfile(1L, updateRequest());

        assertThat(updated.getPosition()).isEqualTo("Winger");
        assertThat(updated.getLocation()).isEqualTo("Johannesburg");
        verify(levelPlayScoreService).recalculateForAthlete(11L);
    }

    @Test
    void athleteCannotViewAnotherAthletesFullProfile() {
        AthleteProfile profile = profile(2L);
        when(athleteProfileRepository.findById(11L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> athleteProfileService.getById(1L, UserRole.ATHLETE, 11L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void searchDelegatesToRepositorySpecification() {
        when(athleteProfileRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThat(athleteProfileService.search("Football", "Cape Town", "Striker")).isEmpty();
    }

    private CreateAthleteProfileRequest createRequest() {
        return new CreateAthleteProfileRequest(null, "Football", "Striker", 18, "Male", "Cape Town",
                "CPUT FC", null, "Fast finisher");
    }

    private UpdateAthleteProfileRequest updateRequest() {
        return new UpdateAthleteProfileRequest("Football", "Winger", 19, "Male", "Johannesburg",
                "CPUT FC", null, "Updated bio");
    }

    private AthleteProfile profile(Long userId) {
        return AthleteProfile.create(userId, "Football", "Striker", 18, "Male", "Cape Town",
                "CPUT FC", "Fast finisher");
    }

    private AthleteProfile withId(AthleteProfile profile, Long id) {
        ReflectionTestUtils.setField(profile, "id", id);
        return profile;
    }
}
