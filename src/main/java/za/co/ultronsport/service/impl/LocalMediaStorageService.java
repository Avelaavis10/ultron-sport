package za.co.ultronsport.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.config.storage.MediaStorageProperties;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.MediaStorageProvider;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.MediaAssetRepository;
import za.co.ultronsport.service.MediaStorageService;
import za.co.ultronsport.web.dto.MediaUploadRequest;

@Service
public class LocalMediaStorageService implements MediaStorageService {

    private final MediaAssetRepository mediaAssetRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final MediaStorageProperties properties;

    public LocalMediaStorageService(MediaAssetRepository mediaAssetRepository,
                                    AthleteProfileRepository athleteProfileRepository,
                                    MediaStorageProperties properties) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public MediaAsset store(MediaUploadRequest request) {
        assertAthleteOwnsProfile(request.ownerUserId(), request.athleteProfileId());
        MultipartFile file = request.file();
        validateFile(file);
        MediaStorageProvider provider = properties.getMode();
        if (provider == MediaStorageProvider.S3_TODO || provider == MediaStorageProvider.AZURE_BLOB_TODO) {
            throw new InvalidStateException("Configured storage provider is not implemented for the MVP.");
        }

        String originalFilename = cleanFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + extension(originalFilename);
        byte[] content = readBytes(file);
        String checksum = sha256(content);
        String storageKey = provider == MediaStorageProvider.MOCK ? "mock/" + storedFilename : storedFilename;
        String publicUrl = publicUrl(storedFilename);

        if (provider == MediaStorageProvider.LOCAL) {
            writeLocalFile(storedFilename, content);
        }

        // TODO: Add malware scanning, CDN publication, thumbnails, transcoding, and AI analysis dispatch later.
        MediaAsset asset = MediaAsset.uploaded(request.ownerUserId(), request.athleteProfileId(), originalFilename,
                storedFilename, file.getContentType(), file.getSize(), checksum, provider, storageKey, publicUrl);
        return mediaAssetRepository.save(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset getMetadata(Long mediaId) {
        return mediaAssetRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found: " + mediaId));
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset getMetadataForUser(Long currentUserId, UserRole currentUserRole, Long mediaId) {
        MediaAsset asset = getMetadata(mediaId);
        if (currentUserRole == UserRole.ADMIN || asset.getOwnerUserId().equals(currentUserId)) {
            return asset;
        }
        throw new AccessDeniedException("You can only view your own media metadata.");
    }

    @Override
    @Transactional
    public MediaAsset attachToEvidence(Long mediaId, Long evidenceUploadId) {
        MediaAsset asset = getMetadata(mediaId);
        asset.attachToEvidence(evidenceUploadId);
        return mediaAssetRepository.save(asset);
    }

    @Override
    @Transactional
    public void delete(Long mediaId) {
        MediaAsset asset = getMetadata(mediaId);
        mediaAssetRepository.delete(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public String resolvePublicUrl(Long mediaId) {
        return getMetadata(mediaId).getPublicUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveInternalPath(Long mediaId) {
        MediaAsset asset = getMetadata(mediaId);
        return Path.of(properties.getLocal().getBasePath(), asset.getStoredFilename()).toAbsolutePath().normalize()
                .toString();
    }

    private void assertAthleteOwnsProfile(Long ownerUserId, Long athleteProfileId) {
        AthleteProfile profile = athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        if (!profile.getUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("You can only upload media for your own athlete profile.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidStateException("Media file must not be empty.");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new InvalidStateException("Media file exceeds maximum size of "
                    + properties.getMaxFileSizeBytes() + " bytes.");
        }
        String contentType = file.getContentType();
        Set<String> supportedTypes = properties.getSupportedContentTypes();
        if (contentType == null || !supportedTypes.contains(contentType)) {
            throw new InvalidStateException("Unsupported media content type: " + contentType);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new InvalidStateException("Could not read uploaded media.");
        }
    }

    private void writeLocalFile(String storedFilename, byte[] content) {
        Path basePath = Path.of(properties.getLocal().getBasePath()).toAbsolutePath().normalize();
        Path target = basePath.resolve(storedFilename).normalize();
        if (!target.startsWith(basePath)) {
            throw new InvalidStateException("Invalid storage target.");
        }
        try {
            Files.createDirectories(basePath);
            Files.write(target, content);
        } catch (IOException ex) {
            throw new InvalidStateException("Could not store uploaded media.");
        }
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }

    private String cleanFilename(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "media" : filename);
        String withoutFolders = Path.of(cleaned).getFileName().toString();
        return withoutFolders.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String extension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String publicUrl(String storedFilename) {
        String baseUrl = properties.getPublicBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + storedFilename;
        }
        return baseUrl + "/" + storedFilename;
    }
}
