package za.co.ultronsport.service;

import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.web.dto.CreateVerificationRequest;

public interface VerificationRequestService {
    VerificationRequest create(CreateVerificationRequest request);

    VerificationRequest getById(Long id);

    VerificationRequest approve(Long requestId, String comments);

    VerificationRequest reject(Long requestId, String comments);

    VerificationRequest flag(Long requestId, String comments);
}
