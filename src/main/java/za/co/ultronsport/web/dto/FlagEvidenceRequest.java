package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlagEvidenceRequest(
        @NotBlank @Size(max = 1200) String reason
) {
}
