package za.co.ultronsport.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import za.co.ultronsport.domain.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, Long>,
        JpaSpecificationExecutor<Organisation> {
    Optional<Organisation> findByNameIgnoreCaseAndLocationIgnoreCase(String name, String location);
}
