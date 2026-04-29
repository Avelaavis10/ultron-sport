package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.service.EvidenceUploadService;
import za.co.ultronsport.web.dto.CreateEvidenceUploadRequest;

@Service
public class EvidenceUploadServiceImpl implements EvidenceUploadService {

    private final EvidenceUploadRepository evidenceUploadRepository;

    public EvidenceUploadServiceImpl(EvidenceUploadRepository evidenceUploadRepository) {
        this.evidenceUploadRepository = evidenceUploadRepository;
    }

    @Override
    public EvidenceUpload create(CreateEvidenceUploadRequest request) {
        EvidenceUpload evidence = EvidenceUpload.create(request.uploadedByUserId(), request.athleteProfileId(),
                request.evidenceType(), request.sport(), request.position(), request.eventType(),
                request.uploadDate(), request.evidenceContext(), request.fileUrl(), request.externalLink(),
                request.metadataNotes());
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    public EvidenceUpload getById(Long id) {
        return evidenceUploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence upload not found: " + id));
    }

    @Override
    public List<EvidenceUpload> listForAthlete(Long athleteProfileId) {
        return evidenceUploadRepository.findByAthleteProfileId(athleteProfileId);
    }

    @Override
    public EvidenceUpload markVerified(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        evidence.markVerified();
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    public EvidenceUpload markRejected(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        evidence.markRejected();
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    public EvidenceUpload flag(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        evidence.flag();
        return evidenceUploadRepository.save(evidence);
    }
}
