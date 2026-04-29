package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.web.dto.CreateEvidenceUploadRequest;

public interface EvidenceUploadService {
    EvidenceUpload create(CreateEvidenceUploadRequest request);

    EvidenceUpload getById(Long id);

    List<EvidenceUpload> listForAthlete(Long athleteProfileId);

    EvidenceUpload markVerified(Long evidenceUploadId);

    EvidenceUpload markRejected(Long evidenceUploadId);

    EvidenceUpload flag(Long evidenceUploadId);
}
