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
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.LevelPlayScoreService;

@SpringBootTest
@AutoConfigureMockMvc
class LevelPlayIntegrationTest {

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
    private LevelPlayScoreService levelPlayScoreService;

    @BeforeEach
    void setUp() {
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        levelPlayScoreRepository.deleteAll();
        achievementRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void athleteCanViewOwnLevelPlayScoreThroughMe() throws Exception {
        RegisteredUser athlete = register("levelplay-athlete@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete, "Own Score Athlete");

        mockMvc.perform(get("/api/levelplay/me")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.finalCredibilityScore").value(16))
                .andExpect(jsonPath("$.tier").value("BRONZE"));
    }

    @Test
    void scoutAgentCanViewLevelPlayScoreForAthlete() throws Exception {
        RegisteredUser scout = register("levelplay-scout@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Scout Visible Athlete");

        mockMvc.perform(get("/api/levelplay/athletes/{athleteProfileId}", profile.getId())
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteProfileId").value(profile.getId()));
    }

    @Test
    void organisationCanViewLevelPlayScoreForAthlete() throws Exception {
        RegisteredUser organisation = register("levelplay-org@example.com", "ORGANISATION");
        AthleteProfile profile = athleteProfile("Organisation Visible Athlete");

        mockMvc.perform(get("/api/levelplay/athletes/{athleteProfileId}", profile.getId())
                .header("Authorization", "Bearer " + organisation.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompletenessScore").value(78));
    }

    @Test
    void coachCanViewLevelPlayScoreForAthlete() throws Exception {
        RegisteredUser coach = register("levelplay-coach@example.com", "COACH");
        AthleteProfile profile = athleteProfile("Coach Visible Athlete");

        mockMvc.perform(get("/api/levelplay/athletes/{athleteProfileId}", profile.getId())
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier").value("BRONZE"));
    }

    @Test
    void adminCanRecalculateOneAthleteScore() throws Exception {
        RegisteredUser admin = register("levelplay-admin@example.com", "ADMIN");
        AthleteProfile profile = athleteProfile("Admin Recalculated Athlete");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Verified levelplay clip");

        mockMvc.perform(post("/api/levelplay/athletes/{athleteProfileId}/recalculate", profile.getId())
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedEvidenceCount").value(1))
                .andExpect(jsonPath("$.evidenceScore").value(20))
                .andExpect(jsonPath("$.finalCredibilityScore").value(38));
    }

    @Test
    void nonAdminCannotRecalculateAthleteScore() throws Exception {
        RegisteredUser scout = register("levelplay-nonadmin@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Blocked Recalculate Athlete");

        mockMvc.perform(post("/api/levelplay/athletes/{athleteProfileId}/recalculate", profile.getId())
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessLevelPlayEndpoints() throws Exception {
        AthleteProfile profile = athleteProfile("Anonymous Blocked Athlete");

        mockMvc.perform(get("/api/levelplay/athletes/{athleteProfileId}", profile.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyingEvidenceTriggersScoreRecalculation() throws Exception {
        RegisteredUser athlete = register("levelplay-workflow-athlete@example.com", "ATHLETE");
        RegisteredUser coach = register("levelplay-workflow-coach@example.com", "COACH");
        AthleteProfile profile = createAthleteProfile(athlete, "Workflow Scored Athlete");
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);

        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        LevelPlayScore score = levelPlayScoreRepository.findByAthleteProfileId(profile.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(score.getVerifiedEvidenceCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(score.getCoachVerificationCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(score.getFinalCredibilityScore()).isEqualTo(48);
    }

    @Test
    void discoveryAthleteCardsIncludeLevelPlayScoreAndTier() throws Exception {
        RegisteredUser scout = register("levelplay-discovery-scout@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Discovery Scored Athlete");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Discovery verified clip");
        levelPlayScoreService.recalculateForAthlete(profile.getId());

        mockMvc.perform(get("/api/discovery/athletes")
                .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].levelPlayScore").value(38))
                .andExpect(jsonPath("$.content[0].levelPlayTier").value("SILVER"));
    }

    @Test
    void scoreExplanationEndpointReturnsExpectedBreakdown() throws Exception {
        RegisteredUser scout = register("levelplay-explain-scout@example.com", "SCOUT_AGENT");
        AthleteProfile profile = athleteProfile("Explained Score Athlete");
        saveEvidence(profile, VerificationStatus.VERIFIED, "Explained verified clip");

        mockMvc.perform(get("/api/levelplay/athletes/{athleteProfileId}/explain", profile.getId())
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.verifiedEvidenceCount").value(1))
                .andExpect(jsonPath("$.verifiedEvidenceCountScore").value(20))
                .andExpect(jsonPath("$.profileCompletenessContribution").value(18))
                .andExpect(jsonPath("$.finalCredibilityScore").value(38))
                .andExpect(jsonPath("$.tier").value("SILVER"));
    }

    private Long createEvidence(RegisteredUser athlete, Long athleteProfileId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/evidence")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(createEvidenceJson(athleteProfileId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private void submitEvidence(RegisteredUser athlete, Long evidenceId) throws Exception {
        mockMvc.perform(post("/api/evidence/{id}/submit", evidenceId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING_VERIFICATION"));
    }

    private EvidenceUpload saveEvidence(AthleteProfile profile, VerificationStatus status, String title) {
        EvidenceUpload evidence = EvidenceUpload.createDraft(profile.getUserId(), profile.getId(), title,
                title + " description", "Football", "Striker", "Match highlight", EvidenceContext.MATCH,
                LocalDate.now(), null, "https://video.example/" + title.replace(" ", "-"));
        if (status == VerificationStatus.VERIFIED) {
            evidence.submit();
            evidence.verify();
        }
        return evidenceUploadRepository.save(evidence);
    }

    private AthleteProfile athleteProfile(String displayName) {
        User user = User.create(displayName, displayName.toLowerCase().replace(" ", "-") + "@example.com",
                null, "hashed-password", UserRole.ATHLETE);
        user.activate();
        User savedUser = userRepository.save(user);
        return athleteProfileRepository.save(AthleteProfile.create(savedUser.getId(), "Football", "Striker",
                18, "Male", "Cape Town", "Ultron Academy", "Complete athlete profile"));
    }

    private AthleteProfile createAthleteProfile(RegisteredUser athlete, String displayName) {
        return athleteProfileRepository.save(AthleteProfile.create(athlete.userId(), "Football", "Striker",
                18, "Male", "Cape Town", "Ultron Academy", "Complete athlete profile"));
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

    private String createEvidenceJson(Long athleteProfileId) {
        return """
                {
                  "athleteProfileId": %d,
                  "title": "Goal highlight",
                  "description": "Cup final goal from the right channel",
                  "sport": "Football",
                  "position": "Striker",
                  "eventType": "Match highlight",
                  "matchOrTraining": "MATCH",
                  "eventDate": "%s",
                  "fileUrl": null,
                  "externalVideoLink": "https://video.example/highlight"
                }
                """.formatted(athleteProfileId, LocalDate.now());
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
