package za.co.ultronsport.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import za.co.ultronsport.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserReturnsJwtAndStoresHashedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerJson("Avela Athlete", "athlete@example.com", "ATHLETE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("athlete@example.com"))
                .andExpect(jsonPath("$.role").value("ATHLETE"));

        User saved = userRepository.findByEmailIgnoreCase("athlete@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(saved.getPasswordHash()).startsWith("$2");
    }

    @Test
    void loginWithValidCredentialsReturnsJwt() throws Exception {
        registerAndReturnToken("coach@example.com", "COACH");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "coach@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("COACH"));
    }

    @Test
    void loginWithInvalidCredentialsIsRejected() throws Exception {
        registerAndReturnToken("scout@example.com", "SCOUT_AGENT");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "scout@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAcceptsValidToken() throws Exception {
        String token = registerAndReturnToken("me@example.com", "ATHLETE");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.role").value("ATHLETE"));
    }

    @Test
    void roleProtectedEndpointRejectsWrongRole() throws Exception {
        String athleteToken = registerAndReturnToken("blocked-athlete@example.com", "ATHLETE");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + athleteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleProtectedEndpointAllowsAdminRole() throws Exception {
        String adminToken = registerAndReturnToken("admin@example.com", "ADMIN");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));
    }

    private String registerAndReturnToken(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerJson("Test User", email, role)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private String registerJson(String displayName, String email, String role) {
        return """
                {
                  "displayName": "%s",
                  "email": "%s",
                  "phone": null,
                  "password": "password123",
                  "role": "%s"
                }
                """.formatted(displayName, email, role);
    }
}
