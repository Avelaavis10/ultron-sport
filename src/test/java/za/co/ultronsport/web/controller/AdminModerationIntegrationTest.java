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
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AdminModerationIntegrationTest {

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
    private AthleteProfileRepository athleteProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        adminActionLogRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        levelPlayScoreRepository.deleteAll();
        achievementRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void adminCanViewAuditLogs() throws Exception {
        RegisteredUser admin = register("admin-audit-view@example.com", "ADMIN");
        EvidenceUpload evidence = saveEvidence(VerificationStatus.DRAFT);
        flagEvidence(admin, evidence.getId());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].actionType").value("EVIDENCE_FLAGGED"));
    }

    @Test
    void nonAdminCannotViewAuditLogs() throws Exception {
        RegisteredUser scout = register("scout-audit-blocked@example.com", "SCOUT_AGENT");

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotViewAuditLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanViewFlaggedEvidence() throws Exception {
        RegisteredUser admin = register("admin-flagged-view@example.com", "ADMIN");
        saveEvidence(VerificationStatus.FLAGGED);

        mockMvc.perform(get("/api/admin/moderation/evidence/flagged")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verificationStatus").value("FLAGGED"));
    }

    @Test
    void adminCanViewArchivedEvidence() throws Exception {
        RegisteredUser admin = register("admin-archived-view@example.com", "ADMIN");
        saveEvidence(VerificationStatus.ARCHIVED);

        mockMvc.perform(get("/api/admin/moderation/evidence/archived")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verificationStatus").value("ARCHIVED"));
    }

    @Test
    void adminCanCreateModerationNote() throws Exception {
        RegisteredUser admin = register("admin-note@example.com", "ADMIN");
        EvidenceUpload evidence = saveEvidence(VerificationStatus.FLAGGED);

        mockMvc.perform(post("/api/admin/moderation/evidence/{evidenceId}/note", evidence.getId())
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Manual review",
                                  "details": "Coach credentials should be checked before appeal."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionType").value("MODERATION_NOTE_CREATED"))
                .andExpect(jsonPath("$.targetType").value("EVIDENCE"));
    }

    @Test
    void flaggingEvidenceCreatesVisibleAuditLog() throws Exception {
        RegisteredUser admin = register("admin-flag-log@example.com", "ADMIN");
        EvidenceUpload evidence = saveEvidence(VerificationStatus.DRAFT);

        flagEvidence(admin, evidence.getId());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("actionType", "EVIDENCE_FLAGGED")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].targetId").value(evidence.getId()))
                .andExpect(jsonPath("$.content[0].reason").value("Potential duplicate evidence"));
    }

    @Test
    void archivingEvidenceCreatesVisibleAuditLog() throws Exception {
        RegisteredUser admin = register("admin-archive-log@example.com", "ADMIN");
        EvidenceUpload evidence = saveEvidence(VerificationStatus.DRAFT);

        mockMvc.perform(post("/api/evidence/{id}/archive", evidence.getId())
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("ARCHIVED"));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("actionType", "EVIDENCE_ARCHIVED")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].targetId").value(evidence.getId()));
    }

    @Test
    void levelPlayRecalculationCreatesVisibleAuditLog() throws Exception {
        RegisteredUser admin = register("admin-levelplay-log@example.com", "ADMIN");
        AthleteProfile profile = athleteProfile("LevelPlay Audit Athlete");

        mockMvc.perform(post("/api/levelplay/athletes/{athleteProfileId}/recalculate", profile.getId())
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("actionType", "LEVELPLAY_RECALCULATED")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].details").value(
                        org.hamcrest.Matchers.containsString("athleteProfileId=" + profile.getId())));
    }

    @Test
    void auditLogResponseDoesNotExposeSensitiveFields() throws Exception {
        RegisteredUser admin = register("admin-sensitive@example.com", "ADMIN");
        EvidenceUpload evidence = saveEvidence(VerificationStatus.DRAFT);
        flagEvidence(admin, evidence.getId());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.content[0].accessToken").doesNotExist())
                .andExpect(jsonPath("$.content[0].token").doesNotExist());
    }

    private void flagEvidence(RegisteredUser admin, Long evidenceId) throws Exception {
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

    private EvidenceUpload saveEvidence(VerificationStatus status) {
        AthleteProfile profile = athleteProfile("Moderation Athlete " + status.name());
        EvidenceUpload evidence = EvidenceUpload.createDraft(profile.getUserId(), profile.getId(),
                status.name() + " clip", "Moderation clip", "Football", "Striker", "Match highlight",
                EvidenceContext.MATCH, LocalDate.now(), null,
                "https://video.example/" + status.name().toLowerCase());
        switch (status) {
            case FLAGGED -> evidence.flag();
            case ARCHIVED -> evidence.archive();
            case PENDING_VERIFICATION -> evidence.submit();
            case VERIFIED -> {
                evidence.submit();
                evidence.verify();
            }
            case REJECTED -> {
                evidence.submit();
                evidence.reject();
            }
            case DRAFT, SUBMITTED -> {
            }
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
