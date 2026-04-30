package za.co.ultronsport.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.OrganisationService;
import za.co.ultronsport.web.dto.CreateOrganisationRequest;
import za.co.ultronsport.web.dto.UpdateOrganisationRequest;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationServiceImpl(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    @Override
    @Transactional
    public Organisation create(Long currentUserId, CreateOrganisationRequest request) {
        String name = requireText(request.name(), "Organisation name is required.");
        String type = requireText(request.type(), "Organisation type is required.");
        String location = requireText(request.location(), "Organisation location is required.");
        organisationRepository.findByNameIgnoreCaseAndLocationIgnoreCase(name, location)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Organisation already exists for this location.");
                });
        Long primaryAdminUserId = request.primaryAdminUserId() == null ? currentUserId : request.primaryAdminUserId();
        return organisationRepository.save(Organisation.create(name, type, location, clean(request.contactEmail()),
                primaryAdminUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Organisation getById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Organisation> search(String name, String type, String location, VerificationStatus verificationStatus,
                                     Pageable pageable) {
        return organisationRepository.findAll(searchSpec(name, type, location, verificationStatus), pageable);
    }

    @Override
    @Transactional
    public Organisation update(Long organisationId, UpdateOrganisationRequest request) {
        Organisation organisation = getById(organisationId);
        String name = hasText(request.name()) ? request.name().trim() : organisation.getName();
        String type = hasText(request.type()) ? request.type().trim() : organisation.getType();
        String location = hasText(request.location()) ? request.location().trim() : organisation.getLocation();
        String contactEmail = request.contactEmail() == null ? organisation.getContactEmail() : clean(request.contactEmail());
        VerificationStatus status = request.verificationStatus() == null
                ? organisation.getVerificationStatus()
                : request.verificationStatus();
        organisation.updateDetails(name, type, location, contactEmail, status);
        return organisationRepository.save(organisation);
    }

    private Specification<Organisation> searchSpec(String name, String type, String location,
                                                   VerificationStatus verificationStatus) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addLike(criteriaBuilder, predicates, root.get("name"), name);
            addLike(criteriaBuilder, predicates, root.get("type"), type);
            addLike(criteriaBuilder, predicates, root.get("location"), location);
            if (verificationStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("verificationStatus"), verificationStatus));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void addLike(jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                         List<Predicate> predicates,
                         jakarta.persistence.criteria.Expression<String> expression,
                         String value) {
        if (hasText(value)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(expression),
                    "%" + value.trim().toLowerCase() + "%"));
        }
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) {
            throw new InvalidStateException(message);
        }
        return value.trim();
    }

    private String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
