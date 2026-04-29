package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.impl.DiscoveryServiceImpl;
import za.co.ultronsport.web.dto.AthleteSearchCriteria;
import za.co.ultronsport.web.dto.EvidenceDiscoveryCardResponse;
import za.co.ultronsport.web.dto.PageResponse;

@DataJpaTest
@Import(DiscoveryServiceImpl.class)
class DiscoveryServiceImplTest {

    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AthleteProfileRepository athleteProfileRepository;

    @Autowired
    private EvidenceUploadRepository evidenceUploadRepository;

    @Test
    void searchReturnsOnlyVerifiedEvidenceForScoutAgent() {
        SecurityUser scout = securityUser("scout-unit@example.com", "Scout Unit", UserRole.SCOUT_AGENT);
        AthleteProfile visibleProfile = athleteProfile("Verified Athlete", "Football", "Striker", "Cape Town");
        AthleteProfile hiddenProfile = athleteProfile("Draft Athlete", "Football", "Striker", "Cape Town");
        saveEvidence(visibleProfile, VerificationStatus.VERIFIED, "Verified goal", "Football", "Striker");
        saveEvidence(hiddenProfile, VerificationStatus.DRAFT, "Draft goal", "Football", "Striker");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(scout,
                criteria(null, null, null, 0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void searchReturnsOnlyVerifiedEvidenceForOrganisation() {
        SecurityUser organisation = securityUser("org-unit@example.com", "Org Unit", UserRole.ORGANISATION);
        AthleteProfile visibleProfile = athleteProfile("Verified Org Athlete", "Rugby", "Wing", "Durban");
        AthleteProfile pendingProfile = athleteProfile("Pending Org Athlete", "Rugby", "Wing", "Durban");
        saveEvidence(visibleProfile, VerificationStatus.VERIFIED, "Verified try", "Rugby", "Wing");
        saveEvidence(pendingProfile, VerificationStatus.PENDING_VERIFICATION, "Pending try", "Rugby", "Wing");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(organisation,
                criteria("Rugby", null, null, 0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("Verified try");
    }

    @Test
    void adminCanSearchAllStatuses() {
        SecurityUser admin = securityUser("admin-unit@example.com", "Admin Unit", UserRole.ADMIN);
        AthleteProfile profile = athleteProfile("Admin Athlete", "Football", "Midfielder", "Johannesburg");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified clip", "Football", "Midfielder");
        saveEvidence(profile, VerificationStatus.DRAFT, "Draft clip", "Football", "Midfielder");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(admin,
                criteria("Football", null, null, 0, 20));

        assertThat(response.content())
                .extracting(EvidenceDiscoveryCardResponse::verificationStatus)
                .containsExactlyInAnyOrder(VerificationStatus.VERIFIED, VerificationStatus.DRAFT);
    }

    @Test
    void keywordFilterWorks() {
        SecurityUser scout = securityUser("keyword-scout@example.com", "Keyword Scout", UserRole.SCOUT_AGENT);
        AthleteProfile profile = athleteProfile("Keyword Athlete", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Free kick goal", "Football", "Striker");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Sprint drill", "Football", "Striker");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(scout,
                criteria(null, null, "free kick", 0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("Free kick goal");
    }

    @Test
    void sportFilterWorks() {
        SecurityUser scout = securityUser("sport-scout@example.com", "Sport Scout", UserRole.SCOUT_AGENT);
        saveEvidence(athleteProfile("Football Athlete", "Football", "Striker", "Cape Town"),
                VerificationStatus.VERIFIED, "Football clip", "Football", "Striker");
        saveEvidence(athleteProfile("Basketball Athlete", "Basketball", "Guard", "Cape Town"),
                VerificationStatus.VERIFIED, "Basketball clip", "Basketball", "Guard");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(scout,
                criteria("Basketball", null, null, 0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).sport()).isEqualTo("Basketball");
    }

    @Test
    void positionFilterWorks() {
        SecurityUser scout = securityUser("position-scout@example.com", "Position Scout", UserRole.SCOUT_AGENT);
        saveEvidence(athleteProfile("Striker Athlete", "Football", "Striker", "Cape Town"),
                VerificationStatus.VERIFIED, "Striker clip", "Football", "Striker");
        saveEvidence(athleteProfile("Keeper Athlete", "Football", "Goalkeeper", "Cape Town"),
                VerificationStatus.VERIFIED, "Keeper clip", "Football", "Goalkeeper");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(scout,
                criteria(null, "Goalkeeper", null, 0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).position()).isEqualTo("Goalkeeper");
    }

    @Test
    void paginationWorks() {
        SecurityUser scout = securityUser("pagination-scout@example.com", "Pagination Scout", UserRole.SCOUT_AGENT);
        AthleteProfile profile = athleteProfile("Paged Athlete", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Clip one", "Football", "Striker");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Clip two", "Football", "Striker");

        PageResponse<EvidenceDiscoveryCardResponse> response = discoveryService.searchEvidence(scout,
                criteria("Football", null, null, 0, 1));

        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void invalidPageSizeIsRejected() {
        SecurityUser scout = securityUser("size-scout@example.com", "Size Scout", UserRole.SCOUT_AGENT);

        assertThatThrownBy(() -> discoveryService.searchEvidence(scout, criteria(null, null, null, 0, 51)))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Page size must be between 1 and 50.");
    }

    @Test
    void invalidSortFieldIsRejected() {
        SecurityUser scout = securityUser("sort-scout@example.com", "Sort Scout", UserRole.SCOUT_AGENT);
        AthleteSearchCriteria badSort = new AthleteSearchCriteria(null, null, null, null, null, null,
                null, null, null, null, 0, 20, "passwordHash", "DESC");

        assertThatThrownBy(() -> discoveryService.searchAthletes(scout, badSort))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Invalid sort field: passwordHash");
    }

    @Test
    void athleteDiscoveryProfileHidesNonVerifiedEvidenceFromScoutAgent() {
        SecurityUser scout = securityUser("hidden-scout@example.com", "Hidden Scout", UserRole.SCOUT_AGENT);
        AthleteProfile profile = athleteProfile("Hidden Athlete", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.PENDING_VERIFICATION, "Pending private clip", "Football", "Striker");

        assertThatThrownBy(() -> discoveryService.getAthleteProfile(scout, profile.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private AthleteSearchCriteria criteria(String sport, String position, String keyword, int page, int size) {
        return new AthleteSearchCriteria(sport, position, null, null, null, null, null, null,
                null, keyword, page, size, "updatedAt", "DESC");
    }

    private SecurityUser securityUser(String email, String displayName, UserRole role) {
        User user = User.create(displayName, email, null, "hashed-password", role);
        user.activate();
        return SecurityUser.from(userRepository.save(user));
    }

    private AthleteProfile athleteProfile(String displayName, String sport, String position, String location) {
        User user = User.create(displayName, displayName.toLowerCase().replace(" ", "-") + "@example.com",
                null, "hashed-password", UserRole.ATHLETE);
        user.activate();
        User savedUser = userRepository.save(user);
        return athleteProfileRepository.save(AthleteProfile.create(savedUser.getId(), sport, position, 18,
                "Unspecified", location, "Ultron Academy", "Athlete bio"));
    }

    private EvidenceUpload saveEvidence(AthleteProfile profile, VerificationStatus status, String title, String sport,
                                        String position) {
        EvidenceUpload evidence = EvidenceUpload.createDraft(profile.getUserId(), profile.getId(), title,
                title + " description", sport, position, "Match highlight", EvidenceContext.MATCH,
                LocalDate.now(), null, "https://video.example/" + title.replace(" ", "-"));
        switch (status) {
            case PENDING_VERIFICATION -> evidence.submit();
            case VERIFIED -> {
                evidence.submit();
                evidence.verify();
            }
            case REJECTED -> {
                evidence.submit();
                evidence.reject();
            }
            case FLAGGED -> evidence.flag();
            case ARCHIVED -> evidence.archive();
            case DRAFT, SUBMITTED -> {
            }
        }
        return evidenceUploadRepository.save(evidence);
    }
}
