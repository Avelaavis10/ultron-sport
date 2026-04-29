package za.co.ultronsport.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public interface EvidenceUploadRepository extends JpaRepository<EvidenceUpload, Long> {
    List<EvidenceUpload> findByAthleteProfileId(Long athleteProfileId);

    List<EvidenceUpload> findByAthleteProfileIdOrderByCreatedAtDesc(Long athleteProfileId);

    List<EvidenceUpload> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    long countByAthleteProfileIdAndVerificationStatus(Long athleteProfileId, VerificationStatus status);
}
