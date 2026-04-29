package za.co.ultronsport.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.service.MediaStorageService;
import za.co.ultronsport.web.dto.MediaAssetResponse;
import za.co.ultronsport.web.dto.MediaUploadRequest;
import za.co.ultronsport.web.dto.UploadMediaResponse;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadMediaResponse upload(@RequestParam Long athleteProfileId,
                                      @RequestPart("file") MultipartFile file,
                                      Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return UploadMediaResponse.from(mediaStorageService.store(new MediaUploadRequest(currentUser.getId(),
                athleteProfileId, file)));
    }

    @GetMapping("/{mediaId}")
    public MediaAssetResponse getMetadata(@PathVariable Long mediaId, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return MediaAssetResponse.from(mediaStorageService.getMetadataForUser(currentUser.getId(),
                currentUser.getRole(), mediaId));
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
