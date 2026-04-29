package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.AiAnalysisStatus;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.MediaStorageProvider;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.impl.EvidenceServiceImpl;
import za.co.ultronsport.web.dto.CreateEvidenceRequest;
import za.co.ultronsport.web.dto.FlagEvidenceRequest;
import za.co.ultronsport.web.dto.RejectEvidenceRequest;
import za.co.ultronsport.web.dto.UpdateEvidenceRequest;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceImplTest {

    @Mock
    private EvidenceUploadRepository evidenceUploadRepository;

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private LevelPlayScoreService levelPlayScoreService;

    @Mock
    private AdminActionLogService adminActionLogService;

    @Mock
    private MediaStorageService mediaStorageService;

    @InjectMocks
    private EvidenceServiceImpl evidenceService;

    @Test
    void athleteCanCreateDraftEvidence() {
        CreateEvidenceRequest request = createRequest(11L);
        when(athleteProfileRepository.findById(11L)).thenReturn(Optional.of(athleteProfile(1L)));
        when(evidenceUploadRepository.save(any(EvidenceUpload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceUpload evidence = evidenceService.createDraftEvidence(1L, request);

        assertThat(evidence.getVerificationStatus()).isEqualTo(VerificationStatus.DRAFT);
        assertThat(evidence.getAiAnalysisStatus()).isEqualTo(AiAnalysisStatus.NOT_STARTED);
        assertThat(evidence.getUploadedByUserId()).isEqualTo(1L);
        assertThat(evidence.getExternalVideoLink()).isEqualTo("https://video.example/highlight");
        assertThat(evidence.getFileUrl()).isNull();
    }

    @Test
    void athleteCanSubmitDraftEvidence() {
        EvidenceUpload evidence = draftEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);

        EvidenceUpload submitted = evidenceService.submitEvidence(1L, 7L);

        assertThat(submitted.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING_VERIFICATION);
    }

    @Test
    void submittedEvidenceBecomesPendingVerification() {
        EvidenceUpload evidence = draftEvidence(1L, 11L);

        evidence.submit();

        assertThat(evidence.isPendingVerification()).isTrue();
        assertThat(evidence.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING_VERIFICATION);
    }

    @Test
    void coachCanVerifyPendingVerificationEvidence() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceUpload verified = evidenceService.verifyEvidence(2L, 7L);

        assertThat(verified.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        verify(verificationRequestRepository).save(any(VerificationRequest.class));
        verify(levelPlayScoreService).recalculateForAthlete(11L);
    }

    @Test
    void verifiedEvidenceCannotBeEditedByAthlete() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        evidence.verify();
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));

        assertThatThrownBy(() -> evidenceService.updateEvidence(1L, 7L, updateRequest()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void athleteCanAttachOwnMediaToOwnDraftEvidence() {
        EvidenceUpload evidence = draftEvidence(1L, 11L);
        setId(evidence, 7L);
        MediaAsset media = mediaAsset(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(mediaStorageService.getMetadata(5L)).thenReturn(media);
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);

        EvidenceUpload attached = evidenceService.attachMedia(1L, 7L, 5L);

        assertThat(attached.getMediaAssetId()).isEqualTo(5L);
        assertThat(attached.getFileUrl()).isEqualTo("http://localhost:8080/media/test.mp4");
        assertThat(attached.getExternalVideoLink()).isNull();
        verify(mediaStorageService).attachToEvidence(5L, 7L);
    }

    @Test
    void athleteCannotAttachMediaToVerifiedEvidence() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        evidence.verify();
        MediaAsset media = mediaAsset(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(mediaStorageService.getMetadata(5L)).thenReturn(media);

        assertThatThrownBy(() -> evidenceService.attachMedia(1L, 7L, 5L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Evidence can only attach media while DRAFT or REJECTED.");
    }

    @Test
    void athleteCannotAttachAnotherAthletesMedia() {
        EvidenceUpload evidence = draftEvidence(1L, 11L);
        MediaAsset media = mediaAsset(2L, 12L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(mediaStorageService.getMetadata(5L)).thenReturn(media);

        assertThatThrownBy(() -> evidenceService.attachMedia(1L, 7L, 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void coachCanRejectEvidenceWithReason() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceUpload rejected = evidenceService.rejectEvidence(2L, 7L,
                new RejectEvidenceRequest("Video quality is unclear."));

        assertThat(rejected.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
    }

    @Test
    void rejectingWithoutReasonFails() {
        assertThatThrownBy(() -> evidenceService.rejectEvidence(2L, 7L, new RejectEvidenceRequest(" ")))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Rejection reason is required.");
    }

    @Test
    void adminCanFlagEvidence() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceUpload flagged = evidenceService.flagEvidence(99L, 7L,
                new FlagEvidenceRequest("Possible duplicate evidence."));

        assertThat(flagged.getVerificationStatus()).isEqualTo(VerificationStatus.FLAGGED);
        verify(adminActionLogService).log(eq(99L), eq(AdminActionType.EVIDENCE_FLAGGED),
                eq(AdminTargetType.EVIDENCE), any(), eq("Possible duplicate evidence."), any());
    }

    @Test
    void adminCanArchiveEvidence() {
        EvidenceUpload evidence = pendingEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);

        EvidenceUpload archived = evidenceService.archiveEvidence(99L, 7L);

        assertThat(archived.getVerificationStatus()).isEqualTo(VerificationStatus.ARCHIVED);
        verify(adminActionLogService).log(eq(99L), eq(AdminActionType.EVIDENCE_ARCHIVED),
                eq(AdminTargetType.EVIDENCE), any(), any(), any());
    }

    @Test
    void invalidStatusTransitionsFail() {
        EvidenceUpload evidence = draftEvidence(1L, 11L);
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));

        assertThatThrownBy(() -> evidenceService.verifyEvidence(2L, 7L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Only PENDING_VERIFICATION evidence can be verified.");
    }

    private CreateEvidenceRequest createRequest(Long athleteProfileId) {
        return new CreateEvidenceRequest(athleteProfileId, "Goal highlight", "Cup final goal",
                "Football", "Striker", "Match highlight", EvidenceContext.MATCH, LocalDate.now(),
                null, "https://video.example/highlight");
    }

    private UpdateEvidenceRequest updateRequest() {
        return new UpdateEvidenceRequest("Updated title", "Updated description", "Football", "Striker",
                "Training drill", EvidenceContext.TRAINING, LocalDate.now(), "https://files.example/video", null);
    }

    private EvidenceUpload draftEvidence(Long uploadedByUserId, Long athleteProfileId) {
        return EvidenceUpload.createDraft(uploadedByUserId, athleteProfileId, "Goal highlight", "Cup final goal",
                "Football", "Striker", "Match highlight", EvidenceContext.MATCH, LocalDate.now(),
                null, "https://video.example/highlight");
    }

    private EvidenceUpload pendingEvidence(Long uploadedByUserId, Long athleteProfileId) {
        EvidenceUpload evidence = draftEvidence(uploadedByUserId, athleteProfileId);
        evidence.submit();
        return evidence;
    }

    private AthleteProfile athleteProfile(Long userId) {
        return AthleteProfile.create(userId, "Football", "Striker", 18, "Male", "Cape Town",
                "CPUT FC", "Bio");
    }

    private MediaAsset mediaAsset(Long ownerUserId, Long athleteProfileId) {
        MediaAsset media = MediaAsset.uploaded(ownerUserId, athleteProfileId, "test.mp4", "test.mp4", "video/mp4",
                12L, "checksum", MediaStorageProvider.LOCAL, "test.mp4", "http://localhost:8080/media/test.mp4");
        setId(media, 5L);
        return media;
    }

    private void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
