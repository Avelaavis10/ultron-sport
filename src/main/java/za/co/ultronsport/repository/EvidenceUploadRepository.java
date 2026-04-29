package za.co.ultronsport.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public interface EvidenceUploadRepository extends JpaRepository<EvidenceUpload, Long>,
        JpaSpecificationExecutor<EvidenceUpload> {
    List<EvidenceUpload> findByAthleteProfileId(Long athleteProfileId);

    List<EvidenceUpload> findByAthleteProfileIdOrderByCreatedAtDesc(Long athleteProfileId);

    List<EvidenceUpload> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    List<EvidenceUpload> findByAthleteProfileIdInAndVerificationStatusInOrderByCreatedAtDesc(
            Collection<Long> athleteProfileIds, Collection<VerificationStatus> statuses);

    List<EvidenceUpload> findByAthleteProfileIdAndVerificationStatusInOrderByCreatedAtDesc(
            Long athleteProfileId, Collection<VerificationStatus> statuses);

    long countByAthleteProfileIdAndVerificationStatus(Long athleteProfileId, VerificationStatus status);

    long countByVerificationStatus(VerificationStatus status);
}
