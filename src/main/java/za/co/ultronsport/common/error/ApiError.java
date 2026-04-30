package za.co.ultronsport.common.error;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        String traceId,
        Map<String, String> validationErrors
) {
    public static ApiError of(HttpStatus status, String message, String path, String code,
                              Map<String, String> validationErrors) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, code,
                UUID.randomUUID().toString(), validationErrors == null ? Map.of() : validationErrors);
    }
}
