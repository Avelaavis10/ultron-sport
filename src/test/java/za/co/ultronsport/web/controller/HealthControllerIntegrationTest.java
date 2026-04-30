package za.co.ultronsport.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Ultron Sport API"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void readinessReturnsReadyAndDatabaseUp() throws Exception {
        mockMvc.perform(get("/api/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void versionReturnsConfiguredApplicationMetadata() throws Exception {
        mockMvc.perform(get("/api/health/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Ultron Sport API"))
                .andExpect(jsonPath("$.version").value("0.1.0-mvp"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
