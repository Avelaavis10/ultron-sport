package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Size;

public record VerificationDecisionRequest(
        @Size(max = 1000) String comments
) {
}
