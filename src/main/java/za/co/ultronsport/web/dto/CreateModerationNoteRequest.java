package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateModerationNoteRequest(
        String reason,
        @NotBlank String details
) {
}
