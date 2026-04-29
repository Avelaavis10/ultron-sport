package za.co.ultronsport.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.CoachProfile;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {
    Optional<CoachProfile> findByUserId(Long userId);
}
