package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.service.EvidenceService;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.service.MediaStorageService;
import za.co.ultronsport.web.dto.CreateEvidenceRequest;
import za.co.ultronsport.web.dto.FlagEvidenceRequest;
import za.co.ultronsport.web.dto.RejectEvidenceRequest;
import za.co.ultronsport.web.dto.UpdateEvidenceRequest;

@Service
public class EvidenceServiceImpl implements EvidenceService {

    private final EvidenceUploadRepository evidenceUploadRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final LevelPlayScoreService levelPlayScoreService;
    private final AdminActionLogService adminActionLogService;
    private final MediaStorageService mediaStorageService;

    public EvidenceServiceImpl(EvidenceUploadRepository evidenceUploadRepository,
                               AthleteProfileRepository athleteProfileRepository,
                               VerificationRequestRepository verificationRequestRepository,
                               LevelPlayScoreService levelPlayScoreService,
                               AdminActionLogService adminActionLogService,
                               MediaStorageService mediaStorageService) {
        this.evidenceUploadRepository = evidenceUploadRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.levelPlayScoreService = levelPlayScoreService;
        this.adminActionLogService = adminActionLogService;
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    @Transactional
    public EvidenceUpload createDraftEvidence(Long currentUserId, CreateEvidenceRequest request) {
        assertAthleteOwnsProfile(currentUserId, request.athleteProfileId());
        EvidenceUpload evidence = EvidenceUpload.createDraft(currentUserId, request.athleteProfileId(),
                request.title(), request.description(), request.sport(), request.position(), request.eventType(),
                request.matchOrTraining(), request.eventDate(), request.fileUrl(), request.externalVideoLink());
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceUpload getEvidenceById(Long currentUserId, UserRole currentUserRole, Long evidenceId) {
        EvidenceUpload evidence = getById(evidenceId);
        assertCanViewEvidence(currentUserId, currentUserRole, evidence);
        return evidence;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceUpload> getMyEvidence(Long currentUserId) {
        return athleteProfileRepository.findByUserId(currentUserId)
                .map(profile -> evidenceUploadRepository.findByAthleteProfileIdOrderByCreatedAtDesc(profile.getId()))
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public EvidenceUpload updateEvidence(Long currentUserId, Long evidenceId, UpdateEvidenceRequest request) {
        EvidenceUpload evidence = getById(evidenceId);
        assertEvidenceOwner(currentUserId, evidence);
        applyTransition(() -> evidence.updateDetails(request.title(), request.description(), request.sport(),
                request.position(), request.eventType(), request.matchOrTraining(), request.eventDate(),
                request.fileUrl(), request.externalVideoLink()));
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    @Transactional
    public EvidenceUpload attachMedia(Long currentUserId, Long evidenceId, Long mediaId) {
        EvidenceUpload evidence = getById(evidenceId);
        assertEvidenceOwner(currentUserId, evidence);
        MediaAsset media = mediaStorageService.getMetadata(mediaId);
        if (!media.getOwnerUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only attach your own media.");
        }
        if (!media.getAthleteProfileId().equals(evidence.getAthleteProfileId())) {
            throw new AccessDeniedException("Media can only be attached to evidence for the same athlete profile.");
        }
        applyTransition(() -> evidence.attachMedia(media.getId(), media.getPublicUrl()));
        EvidenceUpload saved = evidenceUploadRepository.save(evidence);
        mediaStorageService.attachToEvidence(media.getId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public EvidenceUpload submitEvidence(Long currentUserId, Long evidenceId) {
        EvidenceUpload evidence = getById(evidenceId);
        assertEvidenceOwner(currentUserId, evidence);
        applyTransition(evidence::submit);
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceUpload> getPendingVerificationEvidence() {
        return evidenceUploadRepository.findByVerificationStatusOrderByCreatedAtDesc(
                VerificationStatus.PENDING_VERIFICATION);
    }

    @Override
    @Transactional
    public EvidenceUpload verifyEvidence(Long coachUserId, Long evidenceId) {
        EvidenceUpload evidence = getById(evidenceId);
        applyTransition(evidence::verify);
        EvidenceUpload saved = evidenceUploadRepository.save(evidence);
        recordVerificationAction(saved, coachUserId, VerificationStatus.VERIFIED, "Verified by coach.");
        levelPlayScoreService.recalculateForAthlete(saved.getAthleteProfileId());
        return saved;
    }

    @Override
    @Transactional
    public EvidenceUpload rejectEvidence(Long coachUserId, Long evidenceId, RejectEvidenceRequest request) {
        String reason = requireReason(request.reason(), "Rejection reason is required.");
        EvidenceUpload evidence = getById(evidenceId);
        applyTransition(evidence::reject);
        EvidenceUpload saved = evidenceUploadRepository.save(evidence);
        recordVerificationAction(saved, coachUserId, VerificationStatus.REJECTED, reason);
        return saved;
    }

    @Override
    @Transactional
    public EvidenceUpload flagEvidence(Long adminUserId, Long evidenceId, FlagEvidenceRequest request) {
        String reason = requireReason(request.reason(), "Flag reason is required.");
        EvidenceUpload evidence = getById(evidenceId);
        applyTransition(evidence::flag);
        EvidenceUpload saved = evidenceUploadRepository.save(evidence);
        recordVerificationAction(saved, adminUserId, VerificationStatus.FLAGGED, reason);
        adminActionLogService.log(adminUserId, AdminActionType.EVIDENCE_FLAGGED, AdminTargetType.EVIDENCE,
                saved.getId(), reason, "Evidence flagged for moderation.");
        return saved;
    }

    @Override
    @Transactional
    public EvidenceUpload archiveEvidence(Long adminUserId, Long evidenceId) {
        EvidenceUpload evidence = getById(evidenceId);
        applyTransition(evidence::archive);
        EvidenceUpload saved = evidenceUploadRepository.save(evidence);
        adminActionLogService.log(adminUserId, AdminActionType.EVIDENCE_ARCHIVED, AdminTargetType.EVIDENCE,
                saved.getId(), null, "Evidence archived by admin.");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationRequest> getVerificationHistory(Long evidenceId) {
        getById(evidenceId);
        return verificationRequestRepository.findByEvidenceUploadIdOrderByCreatedAtDesc(evidenceId);
    }

    private EvidenceUpload getById(Long evidenceId) {
        return evidenceUploadRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found: " + evidenceId));
    }

    private void assertAthleteOwnsProfile(Long currentUserId, Long athleteProfileId) {
        AthleteProfile profile = athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        if (!profile.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only manage evidence for your own athlete profile.");
        }
    }

    private void assertEvidenceOwner(Long currentUserId, EvidenceUpload evidence) {
        if (!evidence.getUploadedByUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only manage your own evidence.");
        }
    }

    private void assertCanViewEvidence(Long currentUserId, UserRole currentUserRole, EvidenceUpload evidence) {
        if (currentUserRole == UserRole.ADMIN) {
            return;
        }
        if (currentUserRole == UserRole.ATHLETE && evidence.getUploadedByUserId().equals(currentUserId)) {
            return;
        }
        if (currentUserRole == UserRole.COACH && evidence.isPendingVerification()) {
            return;
        }
        if ((currentUserRole == UserRole.SCOUT_AGENT || currentUserRole == UserRole.ORGANISATION)
                && evidence.isVerified()) {
            return;
        }
        throw new AccessDeniedException("You are not allowed to view this evidence.");
    }

    private void recordVerificationAction(EvidenceUpload evidence, Long actorUserId, VerificationStatus status,
                                          String comments) {
        VerificationRequest request = VerificationRequest.create(evidence.getId(), evidence.getUploadedByUserId(),
                actorUserId);
        switch (status) {
            case VERIFIED -> request.approve(comments);
            case REJECTED -> request.reject(comments);
            case FLAGGED -> request.flag(comments);
            default -> throw new InvalidStateException("Unsupported verification history status.");
        }
        verificationRequestRepository.save(request);
    }

    private String requireReason(String reason, String message) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidStateException(message);
        }
        return reason.trim();
    }

    private void applyTransition(Runnable transition) {
        try {
            transition.run();
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
    }
}
