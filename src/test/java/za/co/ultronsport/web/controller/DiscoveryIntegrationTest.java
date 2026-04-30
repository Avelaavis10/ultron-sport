package za.co.ultronsport.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class DiscoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AthleteProfileRepository athleteProfileRepository;

    @Autowired
    private EvidenceUploadRepository evidenceUploadRepository;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private LevelPlayScoreRepository levelPlayScoreRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @BeforeEach
    void setUp() {
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        levelPlayScoreRepository.deleteAll();
        achievementRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        organisationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void scoutAgentCanCallAthleteDiscovery() throws Exception {
        RegisteredUser scout = register("scout-discovery@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Visible Athlete", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Visible goal", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/athletes")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayName").value("Visible Athlete"));
    }

    @Test
    void organisationCanCallAthleteDiscovery() throws Exception {
        RegisteredUser organisation = register("org-discovery@example.com", "ORGANISATION");
        AthleteProfile profile = athleteProfile("Organisation Athlete", "Rugby", "Wing", "Durban");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified try", "Rugby", "Wing");

        mockMvc.perform(get("/api/discovery/athletes")
                        .header("Authorization", "Bearer " + organisation.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sport").value("Rugby"));
    }

    @Test
    void athleteCanCallAthleteDiscovery() throws Exception {
        RegisteredUser athlete = register("athlete-discovery@example.com", "ATHLETE");
        AthleteProfile profile = athleteProfile("Peer Athlete", "Cricket", "Bowler", "Gqeberha");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified wicket", "Cricket", "Bowler");

        mockMvc.perform(get("/api/discovery/athletes")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].position").value("Bowler"));
    }

    @Test
    void unauthenticatedUserCannotCallDiscoveryEndpoints() throws Exception {
        mockMvc.perform(get("/api/discovery/athletes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scoutAgentCannotSeeDraftEvidence() throws Exception {
        RegisteredUser scout = register("scout-no-draft@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Draft Hidden", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.DRAFT, "Draft only clip", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/evidence")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void scoutAgentCannotSeePendingVerificationEvidence() throws Exception {
        RegisteredUser scout = register("scout-no-pending@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Pending Hidden", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.PENDING_VERIFICATION, "Pending only clip", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/evidence")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void scoutAgentCanSeeVerifiedEvidence() throws Exception {
        RegisteredUser scout = register("scout-verified@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Verified Visible", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified visible clip", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/evidence")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].verificationStatus").value("VERIFIED"));
    }

    @Test
    void adminCanSeeVerifiedRejectedFlaggedAndArchivedEvidenceWhenFiltering() throws Exception {
        RegisteredUser admin = register("admin-status@example.com", "ADMIN");
        AthleteProfile profile = athleteProfile("Admin Status Athlete", "Football", "Midfielder", "Johannesburg");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified admin clip", "Football", "Midfielder");
        saveEvidence(profile, VerificationStatus.REJECTED, "Rejected admin clip", "Football", "Midfielder");
        saveEvidence(profile, VerificationStatus.FLAGGED, "Flagged admin clip", "Football", "Midfielder");
        saveEvidence(profile, VerificationStatus.ARCHIVED, "Archived admin clip", "Football", "Midfielder");

        assertAdminCanFilterStatus(admin, "VERIFIED");
        assertAdminCanFilterStatus(admin, "REJECTED");
        assertAdminCanFilterStatus(admin, "FLAGGED");
        assertAdminCanFilterStatus(admin, "ARCHIVED");
    }

    @Test
    void searchFiltersReturnExpectedResults() throws Exception {
        RegisteredUser scout = register("scout-filter@example.com", "SCOUT_AGENT");
        AthleteProfile target = athleteProfile("Filter Target", "Basketball", "Guard", "Pretoria");
        AthleteProfile other = athleteProfile("Filter Other", "Football", "Striker", "Cape Town");
        saveEvidence(target, VerificationStatus.VERIFIED, "Three point highlight", "Basketball", "Guard");
        saveEvidence(other, VerificationStatus.VERIFIED, "Goal highlight", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/evidence")
                        .param("sport", "Basketball")
                        .param("position", "Guard")
                        .param("keyword", "three point")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].athleteDisplayName").value("Filter Target"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void paginationMetadataIsReturned() throws Exception {
        RegisteredUser scout = register("scout-page@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Paged Visible", "Football", "Striker", "Cape Town");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Page clip one", "Football", "Striker");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Page clip two", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/evidence")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void discoveryProfileIncludesOrganisationNameFromOrganisationId() throws Exception {
        RegisteredUser scout = register("scout-organisation-name@example.com", "SCOUT_AGENT");
        Organisation organisation = organisationRepository.save(Organisation.create("Cape Talent Club",
                "Club", "Cape Town", null, null));
        AthleteProfile profile = athleteProfile("Organisation Named Athlete", "Football", "Striker",
                "Cape Town", organisation.getId(), "Legacy Club Name");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Organisation named clip", "Football", "Striker");

        mockMvc.perform(get("/api/discovery/athletes/{athleteProfileId}", profile.getId())
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationName").value("Cape Talent Club"));
    }

    private void assertAdminCanFilterStatus(RegisteredUser admin, String statusValue) throws Exception {
        mockMvc.perform(get("/api/discovery/evidence")
                        .param("verificationStatus", statusValue)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].verificationStatus").value(statusValue));
    }

    private RegisteredUser register(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerJson(email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return new RegisteredUser(response.get("accessToken").asText(), user.getId());
    }

    private AthleteProfile athleteProfile(String displayName, String sport, String position, String location) {
        return athleteProfile(displayName, sport, position, location, null, "Ultron Academy");
    }

    private AthleteProfile athleteProfile(String displayName, String sport, String position, String location,
                                          Long organisationId, String schoolOrClub) {
        User user = User.create(displayName, displayName.toLowerCase().replace(" ", "-") + "@example.com",
                null, "hashed-password", za.co.ultronsport.domain.UserRole.ATHLETE);
        user.activate();
        User savedUser = userRepository.save(user);
        return athleteProfileRepository.save(AthleteProfile.create(savedUser.getId(), sport, position, 18,
                "Unspecified", location, schoolOrClub, organisationId, "Bio"));
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

    private String registerJson(String email, String role) {
        return """
                {
                  "displayName": "Test User",
                  "email": "%s",
                  "phone": null,
                  "password": "password123",
                  "role": "%s"
                }
                """.formatted(email, role);
    }

    private record RegisteredUser(String token, Long userId) {
    }
}
