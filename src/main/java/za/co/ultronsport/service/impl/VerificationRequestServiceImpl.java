package za.co.ultronsport.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.EvidenceUploadService;
import za.co.ultronsport.service.VerificationRequestService;
import za.co.ultronsport.web.dto.CreateVerificationRequest;

@Service
public class VerificationRequestServiceImpl implements VerificationRequestService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final EvidenceUploadService evidenceUploadService;

    public VerificationRequestServiceImpl(VerificationRequestRepository verificationRequestRepository,
                                          EvidenceUploadService evidenceUploadService) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.evidenceUploadService = evidenceUploadService;
    }

    @Override
    @Transactional
    public VerificationRequest create(CreateVerificationRequest request) {
        evidenceUploadService.getById(request.evidenceUploadId()).markPendingVerification();
        VerificationRequest verificationRequest = VerificationRequest.create(request.evidenceUploadId(),
                request.requestedByUserId(), request.verifierUserId());
        return verificationRequestRepository.save(verificationRequest);
    }

    @Override
    public VerificationRequest getById(Long id) {
        return verificationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found: " + id));
    }

    @Override
    @Transactional
    public VerificationRequest approve(Long requestId, String comments) {
        VerificationRequest request = getById(requestId);
        applyDecision(() -> request.approve(comments));
        evidenceUploadService.markVerified(request.getEvidenceUploadId());
        return verificationRequestRepository.save(request);
    }

    @Override
    @Transactional
    public VerificationRequest reject(Long requestId, String comments) {
        VerificationRequest request = getById(requestId);
        applyDecision(() -> request.reject(comments));
        evidenceUploadService.markRejected(request.getEvidenceUploadId());
        return verificationRequestRepository.save(request);
    }

    @Override
    @Transactional
    public VerificationRequest flag(Long requestId, String comments) {
        VerificationRequest request = getById(requestId);
        applyDecision(() -> request.flag(comments));
        evidenceUploadService.flag(request.getEvidenceUploadId());
        return verificationRequestRepository.save(request);
    }

    private void applyDecision(Runnable decision) {
        try {
            decision.run();
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
    }
}
