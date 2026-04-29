package za.co.ultronsport.service;

import za.co.ultronsport.domain.EvidenceUpload;

public interface EvidenceUploadService {
    EvidenceUpload getById(Long id);

    EvidenceUpload markVerified(Long evidenceUploadId);

    EvidenceUpload markRejected(Long evidenceUploadId);

    EvidenceUpload flag(Long evidenceUploadId);
}
