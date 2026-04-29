package za.co.ultronsport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
}
