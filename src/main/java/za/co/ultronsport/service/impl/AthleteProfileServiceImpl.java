package za.co.ultronsport.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.service.AthleteProfileService;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;

@Service
public class AthleteProfileServiceImpl implements AthleteProfileService {

    private final AthleteProfileRepository athleteProfileRepository;

    public AthleteProfileServiceImpl(AthleteProfileRepository athleteProfileRepository) {
        this.athleteProfileRepository = athleteProfileRepository;
    }

    @Override
    public AthleteProfile create(CreateAthleteProfileRequest request) {
        AthleteProfile profile = AthleteProfile.create(request.userId(), request.sport(), request.position(),
                request.age(), request.gender(), request.location(), request.schoolOrClub(), request.bio());
        return athleteProfileRepository.save(profile);
    }

    @Override
    public AthleteProfile getById(Long id) {
        return athleteProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + id));
    }

    @Override
    public List<AthleteProfile> search(String sport, String location, String position) {
        return athleteProfileRepository.findAll(searchSpec(sport, location, position));
    }

    private Specification<AthleteProfile> searchSpec(String sport, String location, String position) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(sport)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sport")),
                        "%" + sport.toLowerCase() + "%"));
            }
            if (hasText(location)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }
            if (hasText(position)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("position")),
                        "%" + position.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
