package za.co.ultronsport.web.controller;

import static org.hamcrest.Matchers.hasItem;
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
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.domain.NotificationStatus;
import za.co.ultronsport.domain.NotificationTargetType;
import za.co.ultronsport.domain.NotificationType;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.NotificationRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

    @Autowired
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
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
    void athleteCanGetNotifications() throws Exception {
        RegisteredUser athlete = register("notification-athlete@example.com", "ATHLETE");
        notificationRepository.save(notification(athlete.userId(), NotificationType.SYSTEM));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("SYSTEM"))
                .andExpect(jsonPath("$.content[0].metadataJson").doesNotExist());
    }

    @Test
    void coachCanGetNotifications() throws Exception {
        RegisteredUser coach = register("notification-coach@example.com", "COACH");
        notificationRepository.save(notification(coach.userId(), NotificationType.COACH_PROFILE_UPDATED));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("COACH_PROFILE_UPDATED"));
    }

    @Test
    void unauthenticatedUserCannotAccessNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void athleteCanMarkOwnNotificationAsRead() throws Exception {
        RegisteredUser athlete = register("notification-read@example.com", "ATHLETE");
        Notification notification = notificationRepository.save(notification(athlete.userId(), NotificationType.SYSTEM));

        mockMvc.perform(post("/api/notifications/{notificationId}/read", notification.getId())
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    void athleteCannotMarkAnotherUsersNotificationAsRead() throws Exception {
        RegisteredUser athlete = register("notification-read-blocked@example.com", "ATHLETE");
        RegisteredUser otherAthlete = register("notification-read-owner@example.com", "ATHLETE");
        Notification notification = notificationRepository.save(notification(otherAthlete.userId(),
                NotificationType.SYSTEM));

        mockMvc.perform(post("/api/notifications/{notificationId}/read", notification.getId())
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void unreadCountReturnsCorrectCount() throws Exception {
        RegisteredUser athlete = register("notification-count@example.com", "ATHLETE");
        Notification read = notification(athlete.userId(), NotificationType.SYSTEM);
        read.markRead();
        notificationRepository.save(notification(athlete.userId(), NotificationType.SYSTEM));
        notificationRepository.save(notification(athlete.userId(), NotificationType.ATHLETE_PROFILE_UPDATED));
        notificationRepository.save(read);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));
    }

    @Test
    void coachVerifiesEvidenceAndAthleteReceivesNotification() throws Exception {
        RegisteredUser athlete = register("notification-verify-athlete@example.com", "ATHLETE");
        RegisteredUser coach = register("notification-verify-coach@example.com", "COACH");
        Long profileId = createProfile(athlete);
        Long evidenceId = createEvidence(athlete, profileId);
        submitEvidence(athlete, evidenceId);
        createCoachProfile(coach);

        mockMvc.perform(post("/api/evidence/{id}/verify", evidenceId)
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("EVIDENCE_VERIFIED")));
    }

    @Test
    void coachRejectsEvidenceAndAthleteReceivesNotification() throws Exception {
        RegisteredUser athlete = register("notification-reject-athlete@example.com", "ATHLETE");
        RegisteredUser coach = register("notification-reject-coach@example.com", "COACH");
        Long profileId = createProfile(athlete);
        Long evidenceId = createEvidence(athlete, profileId);
        submitEvidence(athlete, evidenceId);
        createCoachProfile(coach);

        mockMvc.perform(post("/api/evidence/{id}/reject", evidenceId)
                        .header("Authorization", "Bearer " + coach.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Video is unclear"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("EVIDENCE_REJECTED")))
                .andExpect(jsonPath("$.content[*].message", hasItem(org.hamcrest.Matchers.containsString("Video is unclear"))));
    }

    @Test
    void adminFlagsEvidenceAndAthleteReceivesNotification() throws Exception {
        RegisteredUser athlete = register("notification-flag-athlete@example.com", "ATHLETE");
        RegisteredUser admin = register("notification-flag-admin@example.com", "ADMIN");
        Long profileId = createProfile(athlete);
        Long evidenceId = createEvidence(athlete, profileId);

        mockMvc.perform(post("/api/evidence/{id}/flag", evidenceId)
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Duplicate"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("EVIDENCE_FLAGGED")));
    }

    @Test
    void adminArchivesEvidenceAndAthleteReceivesNotification() throws Exception {
        RegisteredUser athlete = register("notification-archive-athlete@example.com", "ATHLETE");
        RegisteredUser admin = register("notification-archive-admin@example.com", "ADMIN");
        Long profileId = createProfile(athlete);
        Long evidenceId = createEvidence(athlete, profileId);

        mockMvc.perform(post("/api/evidence/{id}/archive", evidenceId)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("EVIDENCE_ARCHIVED")));
    }

    @Test
    void profileAchievementAndLevelPlayNotificationsAreCreated() throws Exception {
        RegisteredUser athlete = register("notification-profile-achievement@example.com", "ATHLETE");
        Long profileId = createProfile(athlete);

        mockMvc.perform(patch("/api/athlete-profiles/me")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(profileUpdateJson()))
                .andExpect(status().isOk());
        createAchievement(athlete, profileId);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("ATHLETE_PROFILE_UPDATED")))
                .andExpect(jsonPath("$.content[*].type", hasItem("ACHIEVEMENT_CREATED")))
                .andExpect(jsonPath("$.content[*].type", hasItem("LEVELPLAY_SCORE_CHANGED")));
    }

    private Notification notification(Long recipientUserId, NotificationType type) {
        return Notification.create(recipientUserId, type, "Test notification", "Notification message",
                NotificationTargetType.SYSTEM, 1L, "{\"internal\":\"hidden\"}");
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

    private Long createEvidence(RegisteredUser athlete, Long athleteProfileId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/evidence")
                        .header("Authorization", "Bearer " + athlete.token())
                        .contentType(APPLICATION_JSON)
                        .content(evidenceJson(athleteProfileId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private void submitEvidence(RegisteredUser athlete, Long evidenceId) throws Exception {
        mockMvc.perform(post("/api/evidence/{id}/submit", evidenceId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk());
    }

    private void createCoachProfile(RegisteredUser coach) throws Exception {
        mockMvc.perform(post("/api/coach-profiles")
                        .header("Authorization", "Bearer " + coach.token())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "certificationReference": "SAFA-123",
                                  "organisationId": null,
                                  "organisationName": "Ultron Academy",
                                  "sport": "Football",
                                  "qualificationSummary": "Qualified coach",
                                  "yearsExperience": 4
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

    private String profileUpdateJson() {
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

    private String evidenceJson(Long athleteProfileId) {
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
