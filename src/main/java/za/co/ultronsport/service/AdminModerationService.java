package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.web.dto.CreateModerationNoteRequest;
import za.co.ultronsport.web.dto.ModerationSummaryResponse;

public interface AdminModerationService {
    List<EvidenceUpload> getFlaggedEvidence();

    List<EvidenceUpload> getArchivedEvidence();

    AdminActionLog createEvidenceModerationNote(Long adminUserId, Long evidenceId,
                                                CreateModerationNoteRequest request);

    ModerationSummaryResponse getSummary();
}
