package za.co.ultronsport.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.MediaAssetRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class MediaIntegrationTest {

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
    private MediaAssetRepository mediaAssetRepository;

    @BeforeEach
    void setUp() {
        mediaAssetRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        evidenceUploadRepository.deleteAll();
        athleteProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void athleteCanUploadSupportedMedia() throws Exception {
        RegisteredUser athlete = register("athlete-upload@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);

        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", profile.getId().toString())
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").exists())
                .andExpect(jsonPath("$.publicUrl").exists())
                .andExpect(jsonPath("$.media.contentType").value("video/mp4"));
    }

    @Test
    void unauthenticatedUserCannotUploadMedia() throws Exception {
        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void coachCannotUploadMedia() throws Exception {
        RegisteredUser coach = register("coach-upload@example.com", "COACH");

        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", "1")
                        .header("Authorization", "Bearer " + coach.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void scoutAgentCannotUploadMedia() throws Exception {
        RegisteredUser scout = register("scout-upload@example.com", "SCOUT_AGENT");

        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", "1")
                        .header("Authorization", "Bearer " + scout.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void organisationCannotUploadMedia() throws Exception {
        RegisteredUser organisation = register("organisation-upload@example.com", "ORGANISATION");

        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", "1")
                        .header("Authorization", "Bearer " + organisation.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewMediaMetadata() throws Exception {
        RegisteredUser athlete = register("athlete-admin-media@example.com", "ATHLETE");
        RegisteredUser admin = register("admin-media@example.com", "ADMIN");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long mediaId = uploadMedia(athlete, profile.getId());

        mockMvc.perform(get("/api/media/{mediaId}", mediaId)
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mediaId))
                .andExpect(jsonPath("$.storageKey").doesNotExist())
                .andExpect(jsonPath("$.publicUrl").exists());
    }

    @Test
    void athleteCanAttachUploadedMediaToOwnEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-attach@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long mediaId = uploadMedia(athlete, profile.getId());
        Long evidenceId = createEvidence(athlete, profile.getId());

        mockMvc.perform(post("/api/evidence/{evidenceId}/media/{mediaId}", evidenceId, mediaId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAssetId").value(mediaId))
                .andExpect(jsonPath("$.fileUrl").exists())
                .andExpect(jsonPath("$.externalVideoLink").doesNotExist());
    }

    @Test
    void athleteCannotAttachMediaToSomeoneElsesEvidence() throws Exception {
        RegisteredUser athlete = register("athlete-media-owner@example.com", "ATHLETE");
        RegisteredUser otherAthlete = register("athlete-evidence-owner@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);
        AthleteProfile otherProfile = createAthleteProfile(otherAthlete);
        Long mediaId = uploadMedia(athlete, profile.getId());
        Long otherEvidenceId = createEvidence(otherAthlete, otherProfile.getId());

        mockMvc.perform(post("/api/evidence/{evidenceId}/media/{mediaId}", otherEvidenceId, mediaId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadResponseIncludesMediaIdAndPublicUrl() throws Exception {
        RegisteredUser athlete = register("athlete-upload-shape@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);

        mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", profile.getId().toString())
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").isNumber())
                .andExpect(jsonPath("$.publicUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/media/")));
    }

    @Test
    void evidenceResponseShowsFileUrlAfterMediaAttachment() throws Exception {
        RegisteredUser athlete = register("athlete-fileurl-after-attach@example.com", "ATHLETE");
        AthleteProfile profile = createAthleteProfile(athlete);
        Long mediaId = uploadMedia(athlete, profile.getId());
        Long evidenceId = createEvidence(athlete, profile.getId());

        mockMvc.perform(post("/api/evidence/{evidenceId}/media/{mediaId}", evidenceId, mediaId)
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/media/")))
                .andExpect(jsonPath("$.mediaAssetId").value(mediaId));
    }

    private Long uploadMedia(RegisteredUser athlete, Long athleteProfileId) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/media/upload")
                        .file(mediaFile())
                        .param("athleteProfileId", athleteProfileId.toString())
                        .header("Authorization", "Bearer " + athlete.token()))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("mediaId").asLong();
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

    private MockMultipartFile mediaFile() {
        return new MockMultipartFile("file", "clip.mp4", "video/mp4", "media-content".getBytes());
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
