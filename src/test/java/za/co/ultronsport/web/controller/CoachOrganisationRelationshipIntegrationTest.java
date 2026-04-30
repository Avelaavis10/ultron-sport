package za.co.ultronsport.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CoachOrganisationRelationshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private EvidenceUploadRepository evidenceUploadRepository;

    @Autowired
    private LevelPlayScoreRepository levelPlayScoreRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private AthleteProfileRepository athleteProfileRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        adminActionLogRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        levelPlayScoreRepository.deleteAll();
        achievementRepository.deleteAll();
        coachProfileRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        organisationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void adminCanCreateOrganisation() throws Exception {
        RegisteredUser admin = register("org-admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/organisations")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content(organisationJson("Cape School", "School", "Cape Town")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cape School"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING_VERIFICATION"));
    }

    @Test
    void organisationRoleCanCreateOrganisation() throws Exception {
        RegisteredUser organisationUser = register("org-role@example.com", "ORGANISATION");

        mockMvc.perform(post("/api/organisations")
                        .header("Authorization", "Bearer " + organisationUser.token())
                        .contentType(APPLICATION_JSON)
                        .content(organisationJson("Community Club", "Club", "Durban")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.primaryAdminUserId").value(organisationUser.userId()));
    }

    @Test
    void coachCanCreateGetAndUpdateOwnCoachProfile() throws Exception {
        RegisteredUser admin = register("coach-org-admin@example.com", "ADMIN");
        RegisteredUser coach = register("coach-profile-owner@example.com", "COACH");
        Long organisationId = createOrganisation(admin, "Coach Club");

        mockMvc.perform(post("/api/coach-profiles")
                        .header("Authorization", "Bearer " + coach.token())
                        .contentType(APPLICATION_JSON)
                        .content(coachProfileJson(organisationId, "SAFA-123", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organisationId").value(organisationId));

        mockMvc.perform(get("/api/coach-profiles/me")
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationName").value("Coach Club"));

        mockMvc.perform(patch("/api/coach-profiles/me")
                        .header("Authorization", "Bearer " + coach.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "certificationReference": "SAFA-999",
                                  "organisationId": %d,
                                  "sport": "Football",
                                  "qualificationSummary": "Updated qualification",
                                  "yearsExperience": 8
                                }
                                """.formatted(organisationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationReference").value("SAFA-999"))
                .andExpect(jsonPath("$.yearsExperience").value(8));
    }

    @Test
    void athleteCanLinkOwnProfileToOrganisationAndLevelPlayRecalculates() throws Exception {
        RegisteredUser admin = register("athlete-org-admin@example.com", "ADMIN");
        RegisteredUser athlete = register("athlete-org-link@example.com", "ATHLETE");
        Long organisationId = createOrganisation(admin, "Athlete Club");
        createAthleteProfile(athlete);

        mockMvc.perform(patch("/api/athlete-profiles/me/organisation")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "organisationId": %d,
                                  "schoolOrClub": "Athlete Club"
                                }
                                """.formatted(organisationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationId").value(organisationId));

        mockMvc.perform(get("/api/levelplay/me")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompletenessScore").value(78));
    }

    @Test
    void scoutAgentCannotUpdateOrganisationOrCoachProfile() throws Exception {
        RegisteredUser admin = register("blocked-update-admin@example.com", "ADMIN");
        RegisteredUser scout = register("blocked-update-scout@example.com", "SCOUT_AGENT");
        Long organisationId = createOrganisation(admin, "Blocked Club");

        mockMvc.perform(patch("/api/organisations/{id}", organisationId)
                        .header("Authorization", "Bearer " + scout.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Blocked"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/coach-profiles")
                        .header("Authorization", "Bearer " + scout.token())
                        .contentType(APPLICATION_JSON)
                        .content(coachProfileJson(organisationId, "SAFA-123", 3)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessOrganisationManagementEndpoint() throws Exception {
        mockMvc.perform(post("/api/organisations")
                        .contentType(APPLICATION_JSON)
                        .content(organisationJson("Anon Club", "Club", "Cape Town")))
                .andExpect(status().isUnauthorized());
    }

    private Long createOrganisation(RegisteredUser admin, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/organisations")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content(organisationJson(name, "Club", "Cape Town")))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createAthleteProfile(RegisteredUser athlete) throws Exception {
        mockMvc.perform(post("/api/athlete-profiles")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "sport": "Football",
                                  "position": "Striker",
                                  "age": 18,
                                  "gender": "Male",
                                  "location": "Cape Town",
                                  "schoolOrClub": null,
                                  "organisationId": null,
                                  "bio": "Complete athlete profile"
                                }
                                """))
                .andExpect(status().isCreated());
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

    private String organisationJson(String name, String type, String location) {
        return """
                {
                  "name": "%s",
                  "type": "%s",
                  "location": "%s",
                  "contactEmail": "contact@example.com"
                }
                """.formatted(name, type, location);
    }

    private String coachProfileJson(Long organisationId, String certificationReference, int yearsExperience) {
        return """
                {
                  "certificationReference": "%s",
                  "organisationId": %d,
                  "sport": "Football",
                  "qualificationSummary": "Qualified grassroots coach",
                  "yearsExperience": %d
                }
                """.formatted(certificationReference, organisationId, yearsExperience);
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
