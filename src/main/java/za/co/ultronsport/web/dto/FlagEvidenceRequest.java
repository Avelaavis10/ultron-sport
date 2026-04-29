package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;

public record FlagEvidenceRequest(
        @NotBlank String reason
) {
}
