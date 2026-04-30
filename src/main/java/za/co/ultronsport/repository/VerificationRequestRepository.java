package za.co.ultronsport.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByVerifierUserIdAndStatus(Long verifierUserId, VerificationStatus status);

    List<VerificationRequest> findByEvidenceUploadIdOrderByCreatedAtDesc(Long evidenceUploadId);

    Optional<VerificationRequest> findFirstByEvidenceUploadIdOrderByCreatedAtDesc(Long evidenceUploadId);

    long countByEvidenceUploadIdAndStatus(Long evidenceUploadId, VerificationStatus status);

    @Query("""
            select count(request)
            from VerificationRequest request
            where request.status = :status
              and request.evidenceUploadId in (
                  select evidence.id
                  from EvidenceUpload evidence
                  where evidence.athleteProfileId = :athleteProfileId
              )
            """)
    long countByAthleteProfileIdAndStatus(@Param("athleteProfileId") Long athleteProfileId,
                                          @Param("status") VerificationStatus status);
}
