package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.config.storage.MediaStorageProperties;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.MediaStorageProvider;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.MediaAssetRepository;
import za.co.ultronsport.service.impl.LocalMediaStorageService;
import za.co.ultronsport.web.dto.MediaUploadRequest;

@ExtendWith(MockitoExtension.class)
class LocalMediaStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private MediaAssetRepository mediaAssetRepository;

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    private MediaStorageProperties properties;
    private LocalMediaStorageService mediaStorageService;

    @BeforeEach
    void setUp() {
        properties = new MediaStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        properties.setPublicBaseUrl("http://localhost:8080/media");
        mediaStorageService = new LocalMediaStorageService(mediaAssetRepository, athleteProfileRepository, properties);
        when(athleteProfileRepository.findById(11L)).thenReturn(Optional.of(athleteProfile(1L)));
    }

    @Test
    void localMediaStorageStoresMetadataSuccessfully() {
        MockMultipartFile file = file("clip.mp4", "video/mp4", "hello");
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MediaAsset asset = mediaStorageService.store(new MediaUploadRequest(1L, 11L, file));

        assertThat(asset.getOriginalFilename()).isEqualTo("clip.mp4");
        assertThat(asset.getContentType()).isEqualTo("video/mp4");
        assertThat(asset.getFileSizeBytes()).isEqualTo(5L);
        assertThat(asset.getStorageProvider()).isEqualTo(MediaStorageProvider.LOCAL);
        assertThat(asset.getPublicUrl()).startsWith("http://localhost:8080/media/");
    }

    @Test
    void emptyFileIsRejected() {
        MockMultipartFile file = file("empty.mp4", "video/mp4", "");

        assertThatThrownBy(() -> mediaStorageService.store(new MediaUploadRequest(1L, 11L, file)))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Media file must not be empty.");
    }

    @Test
    void unsupportedContentTypeIsRejected() {
        MockMultipartFile file = file("clip.gif", "image/gif", "hello");

        assertThatThrownBy(() -> mediaStorageService.store(new MediaUploadRequest(1L, 11L, file)))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Unsupported media content type: image/gif");
    }

    @Test
    void oversizedFileIsRejected() {
        properties.setMaxFileSizeBytes(4L);
        MockMultipartFile file = file("clip.mp4", "video/mp4", "hello");

        assertThatThrownBy(() -> mediaStorageService.store(new MediaUploadRequest(1L, 11L, file)))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Media file exceeds maximum size of 4 bytes.");
    }

    @Test
    void sha256ChecksumIsGenerated() {
        MockMultipartFile file = file("clip.mp4", "video/mp4", "hello");
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MediaAsset asset = mediaStorageService.store(new MediaUploadRequest(1L, 11L, file));

        assertThat(asset.getChecksumSha256())
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void mediaPublicUrlDoesNotExposeInternalFilesystemPath() {
        MockMultipartFile file = file("clip.mp4", "video/mp4", "hello");
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MediaAsset asset = mediaStorageService.store(new MediaUploadRequest(1L, 11L, file));

        assertThat(asset.getPublicUrl()).doesNotContain(tempDir.toString());
        assertThat(asset.getPublicUrl()).doesNotContain("\\");
    }

    private MockMultipartFile file(String filename, String contentType, String content) {
        return new MockMultipartFile("file", filename, contentType, content.getBytes());
    }

    private AthleteProfile athleteProfile(Long userId) {
        return AthleteProfile.create(userId, "Football", "Striker", 18, "Male", "Cape Town",
                "CPUT FC", "Bio");
    }
}
