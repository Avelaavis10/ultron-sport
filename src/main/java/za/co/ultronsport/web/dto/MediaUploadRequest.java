package za.co.ultronsport.web.dto;

import org.springframework.web.multipart.MultipartFile;

public record MediaUploadRequest(
        Long ownerUserId,
        Long athleteProfileId,
        MultipartFile file
) {
}
