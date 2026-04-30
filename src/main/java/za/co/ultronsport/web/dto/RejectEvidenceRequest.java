package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectEvidenceRequest(
        @NotBlank @Size(max = 1200) String reason
) {
}
