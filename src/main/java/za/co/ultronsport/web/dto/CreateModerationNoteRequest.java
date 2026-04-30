package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateModerationNoteRequest(
        @Size(max = 1200) String reason,
        @NotBlank @Size(max = 2000) String details
) {
}
