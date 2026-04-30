package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

public record MediaUploadRequest(
        @NotNull @Positive Long ownerUserId,
        @NotNull @Positive Long athleteProfileId,
        @NotNull MultipartFile file
) {
}
