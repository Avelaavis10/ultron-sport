package za.co.ultronsport.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByVerifierUserIdAndStatus(Long verifierUserId, VerificationStatus status);

    long countByEvidenceUploadIdAndStatus(Long evidenceUploadId, VerificationStatus status);
}
