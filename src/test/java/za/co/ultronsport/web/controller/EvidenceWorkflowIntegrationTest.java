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
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EvidenceWorkflowIntegrationTest {

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
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @BeforeEach
    void setUp() {
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        coachProfileRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        organisationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void athleteCreatesEvidenceUsingJwtToken() throws Exception {
        RegisteredUser athlete = register("athlete-create@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);

        mockMvc.perform(post("/api/evidence")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(createEvidenceJson(profile.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Goal highlight"))
                .andExpect(jsonPath("$.verificationStatus").value("DRAFT"))
                .andExpect(jsonPath("$.aiAnalysisStatus").value("NOT_STARTED"));
    }

    @Test
    void unauthenticatedUserCannotCreateEvidence() throws Exception {
        mockMvc.perform(post("/api/evidence")
                        .contentType(APPLICATION_JSON)
                        .content(createEvidenceJson(1L)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scoutAgentCannotCreateEvidence() throws Exception {
        RegisteredUser scout = register("scout-create@example.com", "SCOUT_AGENT");

        mockMvc.perform(post("/api/evidence")
                        .header("Authorization", "Bearer " + scout.token())
                        .contentType(APPLICATION_JSON)
                        .content(createEvidenceJson(1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void coachCanAccessPendingVerificationEndpoint() throws Exception {
        RegisteredUser athlete = register("athlete-pending@example.com", "ATHLETE");
        RegisteredUser coach = register("coach-pending@example.com", "COACH");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);

        mockMvc.perform(get("/api/evidence/pending-verification")
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(evidenceId));
    }

    @Test
    void athleteCannotAccessPendingVerificationEndpoint() throws Exception {
        RegisteredUser athlete = register("athlete-blocked-pending@example.com", "ATHLETE");

        mockMvc.perform(get("/api/evidence/pending-verification")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void coachCanVerifyEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-verify@example.com", "ATHLETE");
        RegisteredUser coach = register("coach-verify@example.com", "COACH");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);
        createCoachProfile(coach, null);

        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    void athleteCannotVerifyEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-cannot-verify@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);

        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void coachWithoutCoachProfileReceivesCleanErrorWhenVerifying() throws Exception {
        RegisteredUser athlete = register("athlete-no-coach-profile@example.com", "ATHLETE");
        RegisteredUser coach = register("coach-no-profile@example.com", "COACH");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);

        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Coach profile is required before verifying evidence."));
    }

    @Test
    void verificationContextReturnsAthleteCoachAndOrganisationContext() throws Exception {
        RegisteredUser admin = register("admin-context@example.com", "ADMIN");
        RegisteredUser athlete = register("athlete-context@example.com", "ATHLETE");
        RegisteredUser coach = register("coach-context@example.com", "COACH");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long organisationId = createOrganisation(admin);
        linkAthleteOrganisation(athlete, organisationId);
        createCoachProfile(coach, organisationId);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);
        verifyEvidence(coach, evidenceId);

        mockMvc.perform(get("/api/evidence/{id}/verification-context", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.athleteOrganisationName").value("CPUT FC"))
                .andExpect(jsonPath("$.coachOrganisationName").value("CPUT FC"))
                .andExpect(jsonPath("$.sharedOrganisationContext").value(true));

        mockMvc.perform(get("/api/evidence/{id}/verification-context", evidenceId)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestVerificationStatus").value("VERIFIED"));
    }

    @Test
    void scoutAgentCannotViewInternalVerificationContext() throws Exception {
        RegisteredUser athlete = register("athlete-context-block@example.com", "ATHLETE");
        RegisteredUser scout = register("scout-context-block@example.com", "SCOUT_AGENT");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());

        mockMvc.perform(get("/api/evidence/{id}/verification-context", evidenceId)
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanFlagEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-flag@example.com", "ATHLETE");
        RegisteredUser admin = register("admin-flag@example.com", "ADMIN");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());

        mockMvc.perform(post("/api/evidence/{id}/flag", evidenceId)
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Potential duplicate evidence"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("FLAGGED"));
    }

    @Test
    void scoutAgentCanViewVerifiedEvidenceOnly() throws Exception {
        RegisteredUser athlete = register("athlete-scout-verified@example.com", "ATHLETE");
        RegisteredUser coach = register("coach-scout-verified@example.com", "COACH");
        RegisteredUser scout = register("scout-view@example.com", "SCOUT_AGENT");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long evidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, evidenceId);
        createCoachProfile(coach, null);
        verifyEvidence(coach, evidenceId);

        mockMvc.perform(get("/api/evidence/{id}", evidenceId)
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    void scoutAgentCannotViewDraftOrPendingVerificationEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-scout-blocked@example.com", "ATHLETE");
        RegisteredUser scout = register("scout-blocked@example.com", "SCOUT_AGENT");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long draftEvidenceId = createEvidence(athlete, profile.getId());
        Long pendingEvidenceId = createEvidence(athlete, profile.getId());
        submitEvidence(athlete, pendingEvidenceId);

        mockMvc.perform(get("/api/evidence/{id}", draftEvidenceId)
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/evidence/{id}", pendingEvidenceId)
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());
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

    private void verifyEvidence(RegisteredUser coach, Long evidenceId) throws Exception {
        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    private Long createOrganisation(RegisteredUser admin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/organisations")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CPUT FC",
                                  "type": "Club",
                                  "location": "Cape Town",
                                  "contactEmail": "club@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("CPUT FC"))
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private void createCoachProfile(RegisteredUser coach, Long organisationId) throws Exception {
        mockMvc.perform(post("/api/coach-profiles")
                        .header("Authorization", "Bearer " + coach.token())
                        .contentType(APPLICATION_JSON)
                        .content(coachProfileJson(organisationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.certificationReference").value("SAFA-123"));
    }

    private void linkAthleteOrganisation(RegisteredUser athlete, Long organisationId) throws Exception {
        mockMvc.perform(patch("/api/athlete-profiles/me/organisation")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "organisationId": %d,
                                  "schoolOrClub": "CPUT FC"
                                }
                                """.formatted(organisationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationId").value(organisationId));
    }

    private AthleteProfile createAthleteProfile(RegisteredUser athlete) {
        return athleteProfileRepository.save(AthleteProfile.create(athlete.userId(), "Football", "Striker", 18,
                "Male", "Cape Town", "CPUT FC", "Bio"));
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

    private String coachProfileJson(Long organisationId) {
        String organisationValue = organisationId == null ? "null" : organisationId.toString();
        return """
                {
                  "certificationReference": "SAFA-123",
                  "organisationId": %s,
                  "organisationName": "CPUT FC",
                  "sport": "Football",
                  "qualificationSummary": "Qualified grassroots coach",
                  "yearsExperience": 5
                }
                """.formatted(organisationValue);
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
