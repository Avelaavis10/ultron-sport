package za.co.ultronsport.service.impl;

import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.service.EvidenceUploadService;

@Service
public class EvidenceUploadServiceImpl implements EvidenceUploadService {

    private final EvidenceUploadRepository evidenceUploadRepository;

    public EvidenceUploadServiceImpl(EvidenceUploadRepository evidenceUploadRepository) {
        this.evidenceUploadRepository = evidenceUploadRepository;
    }

    @Override
    public EvidenceUpload getById(Long id) {
        return evidenceUploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence upload not found: " + id));
    }

    @Override
    public EvidenceUpload markVerified(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        applyTransition(evidence::markVerified);
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    public EvidenceUpload markRejected(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        applyTransition(evidence::markRejected);
        return evidenceUploadRepository.save(evidence);
    }

    @Override
    public EvidenceUpload flag(Long evidenceUploadId) {
        EvidenceUpload evidence = getById(evidenceUploadId);
        applyTransition(evidence::flag);
        return evidenceUploadRepository.save(evidence);
    }

    private void applyTransition(Runnable transition) {
        try {
            transition.run();
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
    }
}
