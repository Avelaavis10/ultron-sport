package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.service.AdminModerationService;
import za.co.ultronsport.web.dto.CreateModerationNoteRequest;
import za.co.ultronsport.web.dto.ModerationSummaryResponse;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final EvidenceUploadRepository evidenceUploadRepository;
    private final AdminActionLogService adminActionLogService;

    public AdminModerationServiceImpl(EvidenceUploadRepository evidenceUploadRepository,
                                      AdminActionLogService adminActionLogService) {
        this.evidenceUploadRepository = evidenceUploadRepository;
        this.adminActionLogService = adminActionLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceUpload> getFlaggedEvidence() {
        return evidenceUploadRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.FLAGGED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceUpload> getArchivedEvidence() {
        return evidenceUploadRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.ARCHIVED);
    }

    @Override
    @Transactional
    public AdminActionLog createEvidenceModerationNote(Long adminUserId, Long evidenceId,
                                                       CreateModerationNoteRequest request) {
        if (request.details() == null || request.details().isBlank()) {
            throw new InvalidStateException("Moderation note details are required.");
        }
        evidenceUploadRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found: " + evidenceId));
        return adminActionLogService.log(adminUserId, AdminActionType.MODERATION_NOTE_CREATED,
                AdminTargetType.EVIDENCE, evidenceId, request.reason(), request.details());
    }

    @Override
    @Transactional(readOnly = true)
    public ModerationSummaryResponse getSummary() {
        return new ModerationSummaryResponse(
                evidenceUploadRepository.countByVerificationStatus(VerificationStatus.FLAGGED),
                evidenceUploadRepository.countByVerificationStatus(VerificationStatus.ARCHIVED),
                evidenceUploadRepository.countByVerificationStatus(VerificationStatus.PENDING_VERIFICATION),
                evidenceUploadRepository.countByVerificationStatus(VerificationStatus.VERIFIED),
                evidenceUploadRepository.countByVerificationStatus(VerificationStatus.REJECTED));
    }
}
