package za.co.ultronsport.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AthleteProfileAchievementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AthleteProfileRepository athleteProfileRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private EvidenceUploadRepository evidenceUploadRepository;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private LevelPlayScoreRepository levelPlayScoreRepository;

    @BeforeEach
    void setUp() {
        levelPlayScoreRepository.deleteAll();
        achievementRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void athleteCanCreateProfileThroughApi() throws Exception {
        RegisteredUser athlete = register("athlete-profile-create@example.com", "ATHLETE");

        mockMvc.perform(post("/api/athlete-profiles")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(profileJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(athlete.userId()))
                .andExpect(jsonPath("$.sport").value("Football"))
                .andExpect(jsonPath("$.profileCompletenessScore").exists());
    }

    @Test
    void athleteCannotCreateDuplicateProfileThroughApi() throws Exception {
        RegisteredUser athlete = register("athlete-profile-duplicate@example.com", "ATHLETE");
        createProfile(athlete);

        mockMvc.perform(post("/api/athlete-profiles")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(profileJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void athleteCanGetMyProfile() throws Exception {
        RegisteredUser athlete = register("athlete-profile-me@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);

        mockMvc.perform(get("/api/athlete-profiles/me")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId))
                .andExpect(jsonPath("$.location").value("Cape Town"));
    }

    @Test
    void athleteCanPatchMyProfile() throws Exception {
        RegisteredUser athlete = register("athlete-profile-patch@example.com", "ATHLETE");
        createProfile(athlete);

        mockMvc.perform(patch("/api/athlete-profiles/me")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(updateProfileJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Winger"))
                .andExpect(jsonPath("$.location").value("Johannesburg"));
    }

    @Test
    void unauthenticatedUserCannotAccessAthleteProfileEndpoints() throws Exception {
        mockMvc.perform(get("/api/athlete-profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scoutAgentCannotUpdateAthleteProfile() throws Exception {
        RegisteredUser scout = register("scout-profile-patch@example.com", "SCOUT_AGENT");

        mockMvc.perform(patch("/api/athlete-profiles/me")
                        .header("Authorization", "Bearer " + scout.token())
                        .contentType(APPLICATION_JSON)
                        .content(updateProfileJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void athleteCanCreateAchievementThroughApi() throws Exception {
        RegisteredUser athlete = register("athlete-achievement-create@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);

        mockMvc.perform(post("/api/achievements")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(achievementJson(profileId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Top Scorer"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void athleteCanListOwnAchievements() throws Exception {
        RegisteredUser athlete = register("athlete-achievement-list@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);
        createAchievement(athlete, profileId);

        mockMvc.perform(get("/api/achievements/my")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Top Scorer"));
    }

    @Test
    void athleteCannotUpdateAnotherAthletesAchievement() throws Exception {
        RegisteredUser athlete = register("athlete-achievement-blocked@example.com", "ATHLETE");
        RegisteredUser otherAthlete = register("other-athlete-achievement@example.com", "ATHLETE");
        createProfile(athlete);
        Long otherProfileId = createProfile(otherAthlete);
        Long otherAchievementId = createAchievement(otherAthlete, otherProfileId);

        mockMvc.perform(patch("/api/achievements/{achievementId}", otherAchievementId)
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(updateAchievementJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void achievementChangesAreReflectedInLevelPlayExplanation() throws Exception {
        RegisteredUser athlete = register("athlete-levelplay-achievement@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);
        createAchievement(athlete, profileId);

        mockMvc.perform(get("/api/levelplay/athletes/{profileId}/explain", profileId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievementCount").value(1))
                .andExpect(jsonPath("$.achievementScore").value(10));
    }

    @Test
    void discoveryProfileIncludesUpdatedAchievementSummary() throws Exception {
        RegisteredUser athlete = register("athlete-discovery-achievement@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);
        createAchievement(athlete, profileId);

        mockMvc.perform(get("/api/discovery/athletes/{profileId}", profileId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievements[0].title").value("Top Scorer"));
    }

    @Test
    void adminCanListAthleteProfiles() throws Exception {
        RegisteredUser athlete = register("athlete-admin-profile-list@example.com", "ATHLETE");
        RegisteredUser admin = register("admin-profile-list@example.com", "ADMIN");
        createProfile(athlete);

        mockMvc.perform(get("/api/athlete-profiles")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sport").value("Football"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private Long createProfile(RegisteredUser athlete) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/athlete-profiles")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(profileJson()))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private Long createAchievement(RegisteredUser athlete, Long athleteProfileId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/achievements")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(achievementJson(athleteProfileId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
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

    private String profileJson() {
        return """
                {
                  "sport": "Football",
                  "position": "Striker",
                  "age": 18,
                  "gender": "Male",
                  "location": "Cape Town",
                  "schoolOrClub": "CPUT FC",
                  "organisationId": null,
                  "bio": "Fast finisher"
                }
                """;
    }

    private String updateProfileJson() {
        return """
                {
                  "sport": "Football",
                  "position": "Winger",
                  "age": 19,
                  "gender": "Male",
                  "location": "Johannesburg",
                  "schoolOrClub": "CPUT FC",
                  "organisationId": null,
                  "bio": "Updated profile bio"
                }
                """;
    }

    private String achievementJson(Long athleteProfileId) {
        return """
                {
                  "athleteProfileId": %d,
                  "title": "Top Scorer",
                  "description": "League top scorer",
                  "achievedAt": "%s"
                }
                """.formatted(athleteProfileId, LocalDate.now());
    }

    private String updateAchievementJson() {
        return """
                {
                  "title": "Updated Achievement",
                  "description": "Updated achievement details",
                  "achievedAt": "%s"
                }
                """.formatted(LocalDate.now());
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
