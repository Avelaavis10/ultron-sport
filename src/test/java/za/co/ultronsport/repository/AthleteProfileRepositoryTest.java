package za.co.ultronsport.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.co.ultronsport.domain.AthleteProfile;

@DataJpaTest
class AthleteProfileRepositoryTest {

    @Autowired
    private AthleteProfileRepository athleteProfileRepository;

    @Test
    void savesAndFindsAthleteProfileByUserId() {
        AthleteProfile saved = athleteProfileRepository.save(AthleteProfile.create(1L, "Football",
                "Striker", 18, "Male", "Cape Town", "CPUT FC", "Bio"));

        assertThat(athleteProfileRepository.findByUserId(1L)).contains(saved);
    }
}
