package za.co.ultronsport.web.dto;

import java.time.Instant;

public record HealthReadinessResponse(
        String status,
        String database,
        Instant timestamp
) {
}
