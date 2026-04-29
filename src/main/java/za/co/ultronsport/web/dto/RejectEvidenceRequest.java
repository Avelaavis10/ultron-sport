package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectEvidenceRequest(
        @NotBlank String reason
) {
}
