package za.co.ultronsport.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ErrorHandlingIntegrationTest {

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
    void validationErrorResponseUsesStandardStructure() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "",
                                  "email": "not-an-email",
                                  "phone": null,
                                  "password": "",
                                  "role": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.role").exists());
    }

    @Test
    void unauthenticatedProtectedEndpointReturnsConsistent401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required."))
                .andExpect(jsonPath("$.path").value("/api/notifications"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void wrongRoleReturnsConsistent403() throws Exception {
        RegisteredUser scout = register("error-scout@example.com", "SCOUT_AGENT");

        mockMvc.perform(post("/api/evidence")
                        .header("Authorization", "Bearer " + scout.token())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied."))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void notFoundReturnsConsistent404() throws Exception {
        RegisteredUser athlete = register("error-athlete@example.com", "ATHLETE");

        mockMvc.perform(post("/api/notifications/{notificationId}/read", 999L)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Notification not found: 999"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void invalidEnumBodyReturnsConsistent400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Bad Role",
                                  "email": "bad-role@example.com",
                                  "phone": null,
                                  "password": "password123",
                                  "role": "NOPE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request or invalid enum value."))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void invalidRequestParameterReturnsConsistent400() throws Exception {
        RegisteredUser athlete = register("error-param-athlete@example.com", "ATHLETE");

        mockMvc.perform(get("/api/notifications?status=maybe")
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid notification status: maybe"))
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    @Test
    void unsupportedMediaTypeReturnsConsistent415() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported media type."))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void methodNotAllowedReturnsConsistent405() throws Exception {
        mockMvc.perform(post("/api/health"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Method not allowed."))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
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
