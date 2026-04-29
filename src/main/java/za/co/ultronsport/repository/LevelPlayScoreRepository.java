package za.co.ultronsport.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.LevelPlayScore;

public interface LevelPlayScoreRepository extends JpaRepository<LevelPlayScore, Long> {
    Optional<LevelPlayScore> findByAthleteProfileId(Long athleteProfileId);

    List<LevelPlayScore> findByAthleteProfileIdIn(Collection<Long> athleteProfileIds);
}
