package za.co.ultronsport.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.AthleteProfileService;
import za.co.ultronsport.service.LevelPlayScoreService;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;
import za.co.ultronsport.web.dto.LinkAthleteOrganisationRequest;
import za.co.ultronsport.web.dto.UpdateAthleteProfileRequest;

@Service
public class AthleteProfileServiceImpl implements AthleteProfileService {

    private final AthleteProfileRepository athleteProfileRepository;
    private final OrganisationRepository organisationRepository;
    private final LevelPlayScoreService levelPlayScoreService;
    private final NotificationService notificationService;

    public AthleteProfileServiceImpl(AthleteProfileRepository athleteProfileRepository,
                                     OrganisationRepository organisationRepository,
                                     LevelPlayScoreService levelPlayScoreService,
                                     NotificationService notificationService) {
        this.athleteProfileRepository = athleteProfileRepository;
        this.organisationRepository = organisationRepository;
        this.levelPlayScoreService = levelPlayScoreService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public AthleteProfile create(Long currentUserId, CreateAthleteProfileRequest request) {
        if (athleteProfileRepository.findByUserId(currentUserId).isPresent()) {
            throw new DuplicateResourceException("Athlete profile already exists for current user.");
        }
        assertOrganisationExists(request.organisationId());
        AthleteProfile profile = AthleteProfile.create(currentUserId, request.sport(), request.position(),
                request.age(), request.gender(), request.location(), request.schoolOrClub(), request.organisationId(),
                request.bio());
        AthleteProfile saved = athleteProfileRepository.save(profile);
        levelPlayScoreService.recalculateForAthlete(saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteProfile getMyProfile(Long currentUserId) {
        return athleteProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found for current user."));
    }

    @Override
    @Transactional
    public AthleteProfile updateMyProfile(Long currentUserId, UpdateAthleteProfileRequest request) {
        AthleteProfile profile = getMyProfile(currentUserId);
        assertOrganisationExists(request.organisationId());
        profile.updateDetails(request.sport(), request.position(), request.age(), request.gender(), request.location(),
                request.schoolOrClub(), request.organisationId(), request.bio());
        AthleteProfile saved = athleteProfileRepository.save(profile);
        levelPlayScoreService.recalculateForAthlete(saved.getId());
        notificationService.notifyAthleteProfileUpdated(saved.getUserId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public AthleteProfile linkOrganisation(Long currentUserId, LinkAthleteOrganisationRequest request) {
        if (request.organisationId() == null && !hasText(request.schoolOrClub())) {
            throw new InvalidStateException("Organisation ID or school/club name is required.");
        }
        assertOrganisationExists(request.organisationId());
        AthleteProfile profile = getMyProfile(currentUserId);
        profile.linkOrganisation(request.organisationId(), clean(request.schoolOrClub()));
        AthleteProfile saved = athleteProfileRepository.save(profile);
        levelPlayScoreService.recalculateForAthlete(saved.getId());
        notificationService.notifyOrganisationLinked(saved.getUserId(), saved.getId(), saved.getOrganisationId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteProfile getById(Long id) {
        return athleteProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteProfile getById(Long currentUserId, UserRole currentUserRole, Long id) {
        AthleteProfile profile = getById(id);
        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.COACH) {
            return profile;
        }
        if (currentUserRole == UserRole.ATHLETE && profile.getUserId().equals(currentUserId)) {
            return profile;
        }
        throw new AccessDeniedException("You are not allowed to view this athlete profile.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AthleteProfile> listAll(Pageable pageable) {
        return athleteProfileRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
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

    private void assertOrganisationExists(Long organisationId) {
        if (organisationId == null) {
            return;
        }
        if (!organisationRepository.existsById(organisationId)) {
            throw new ResourceNotFoundException("Organisation not found: " + organisationId);
        }
    }

    private String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
