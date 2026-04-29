package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.web.dto.CreateEvidenceRequest;
import za.co.ultronsport.web.dto.FlagEvidenceRequest;
import za.co.ultronsport.web.dto.RejectEvidenceRequest;
import za.co.ultronsport.web.dto.UpdateEvidenceRequest;

public interface EvidenceService {
    EvidenceUpload createDraftEvidence(Long currentUserId, CreateEvidenceRequest request);

    EvidenceUpload getEvidenceById(Long currentUserId, UserRole currentUserRole, Long evidenceId);

    List<EvidenceUpload> getMyEvidence(Long currentUserId);

    EvidenceUpload updateEvidence(Long currentUserId, Long evidenceId, UpdateEvidenceRequest request);

    EvidenceUpload attachMedia(Long currentUserId, Long evidenceId, Long mediaId);

    EvidenceUpload submitEvidence(Long currentUserId, Long evidenceId);

    List<EvidenceUpload> getPendingVerificationEvidence();

    EvidenceUpload verifyEvidence(Long coachUserId, Long evidenceId);

    EvidenceUpload rejectEvidence(Long coachUserId, Long evidenceId, RejectEvidenceRequest request);

    EvidenceUpload flagEvidence(Long adminUserId, Long evidenceId, FlagEvidenceRequest request);

    EvidenceUpload archiveEvidence(Long adminUserId, Long evidenceId);

    List<VerificationRequest> getVerificationHistory(Long evidenceId);
}
