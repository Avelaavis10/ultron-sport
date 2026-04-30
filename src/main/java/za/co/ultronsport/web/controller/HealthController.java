package za.co.ultronsport.web.controller;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.UltronAppProperties;
import za.co.ultronsport.service.HealthService;
import za.co.ultronsport.web.dto.HealthReadinessResponse;
import za.co.ultronsport.web.dto.HealthResponse;
import za.co.ultronsport.web.dto.HealthVersionResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;
    private final UltronAppProperties appProperties;

    public HealthController(HealthService healthService, UltronAppProperties appProperties) {
        this.healthService = healthService;
        this.appProperties = appProperties;
    }

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", appProperties.getName(), appProperties.getEnvironment(), Instant.now());
    }

    @GetMapping("/readiness")
    public HealthReadinessResponse readiness() {
        return healthService.readiness();
    }

    @GetMapping("/version")
    public HealthVersionResponse version() {
        return new HealthVersionResponse(appProperties.getName(), appProperties.getVersion(),
                appProperties.getEnvironment(), Instant.now());
    }
}
