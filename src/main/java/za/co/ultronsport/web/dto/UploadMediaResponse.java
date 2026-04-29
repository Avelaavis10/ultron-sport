package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.MediaAsset;

public record UploadMediaResponse(
        Long mediaId,
        String publicUrl,
        MediaAssetResponse media
) {
    public static UploadMediaResponse from(MediaAsset asset) {
        return new UploadMediaResponse(asset.getId(), asset.getPublicUrl(), MediaAssetResponse.from(asset));
    }
}
