package za.co.ultronsport.web.dto;

import java.time.Instant;

public record HealthVersionResponse(
        String application,
        String version,
        String environment,
        Instant timestamp
) {
}
