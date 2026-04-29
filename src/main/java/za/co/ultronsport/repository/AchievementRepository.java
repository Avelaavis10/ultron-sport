package za.co.ultronsport.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByAthleteProfileId(Long athleteProfileId);

    List<Achievement> findByAthleteProfileIdIn(Collection<Long> athleteProfileIds);

    long countByAthleteProfileId(Long athleteProfileId);
}
