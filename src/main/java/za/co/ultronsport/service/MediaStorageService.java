package za.co.ultronsport.service;

import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.web.dto.MediaUploadRequest;

public interface MediaStorageService {
    MediaAsset store(MediaUploadRequest request);

    MediaAsset getMetadata(Long mediaId);

    MediaAsset getMetadataForUser(Long currentUserId, UserRole currentUserRole, Long mediaId);

    MediaAsset attachToEvidence(Long mediaId, Long evidenceUploadId);

    void delete(Long mediaId);

    String resolvePublicUrl(Long mediaId);

    String resolveInternalPath(Long mediaId);
}
