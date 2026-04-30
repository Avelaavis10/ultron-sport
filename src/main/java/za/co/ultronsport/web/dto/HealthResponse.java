package za.co.ultronsport.web.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String application,
        String environment,
        Instant timestamp
) {
}
