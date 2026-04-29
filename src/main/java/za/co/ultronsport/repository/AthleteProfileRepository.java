package za.co.ultronsport.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import za.co.ultronsport.domain.AthleteProfile;

public interface AthleteProfileRepository
        extends JpaRepository<AthleteProfile, Long>, JpaSpecificationExecutor<AthleteProfile> {
    Optional<AthleteProfile> findByUserId(Long userId);
}
