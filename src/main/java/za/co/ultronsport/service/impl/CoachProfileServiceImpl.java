package za.co.ultronsport.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.service.CoachProfileService;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;
import za.co.ultronsport.web.dto.UpdateCoachProfileRequest;

@Service
public class CoachProfileServiceImpl implements CoachProfileService {

    private final CoachProfileRepository coachProfileRepository;
    private final OrganisationRepository organisationRepository;
    private final NotificationService notificationService;

    public CoachProfileServiceImpl(CoachProfileRepository coachProfileRepository,
                                   OrganisationRepository organisationRepository,
                                   NotificationService notificationService) {
        this.coachProfileRepository = coachProfileRepository;
        this.organisationRepository = organisationRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CoachProfile create(Long currentUserId, CreateCoachProfileRequest request) {
        if (coachProfileRepository.existsByUserId(currentUserId)) {
            throw new DuplicateResourceException("Coach profile already exists for current user.");
        }
        Organisation organisation = getOrganisationIfPresent(request.organisationId());
        CoachProfile profile = CoachProfile.create(currentUserId,
                requireText(request.certificationReference(), "Certification reference is required."),
                request.organisationId(), organisationName(request.organisationName(), organisation),
                clean(request.sport()), clean(request.qualificationSummary()), request.yearsExperience());
        CoachProfile saved = coachProfileRepository.save(profile);
        notificationService.notifyCoachProfileUpdated(saved.getUserId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public CoachProfile getMyProfile(Long currentUserId) {
        return coachProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found for current user."));
    }

    @Override
    @Transactional
    public CoachProfile updateMyProfile(Long currentUserId, UpdateCoachProfileRequest request) {
        CoachProfile profile = getMyProfile(currentUserId);
        Organisation organisation = getOrganisationIfPresent(request.organisationId());
        String certificationReference = hasText(request.certificationReference())
                ? request.certificationReference().trim()
                : profile.getCertificationReference();
        Long organisationId = request.organisationId() == null ? profile.getOrganisationId() : request.organisationId();
        String organisationName = request.organisationId() == null
                ? cleanOrExisting(request.organisationName(), profile.getOrganisationName())
                : organisationName(request.organisationName(), organisation);
        String sport = cleanOrExisting(request.sport(), profile.getSport());
        String qualificationSummary = cleanOrExisting(request.qualificationSummary(),
                profile.getQualificationSummary());
        Integer yearsExperience = request.yearsExperience() == null
                ? profile.getYearsExperience()
                : request.yearsExperience();
        profile.updateDetails(certificationReference, organisationId, organisationName, sport, qualificationSummary,
                yearsExperience);
        CoachProfile saved = coachProfileRepository.save(profile);
        notificationService.notifyCoachProfileUpdated(saved.getUserId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public CoachProfile getById(Long id) {
        return coachProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CoachProfile getById(Long currentUserId, UserRole currentUserRole, Long id) {
        CoachProfile profile = getById(id);
        if (currentUserRole == UserRole.ADMIN || profile.getUserId().equals(currentUserId)) {
            return profile;
        }
        throw new AccessDeniedException("You are not allowed to view this coach profile.");
    }

    private Organisation getOrganisationIfPresent(Long organisationId) {
        if (organisationId == null) {
            return null;
        }
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found: " + organisationId));
    }

    private String organisationName(String requestedName, Organisation organisation) {
        if (organisation != null) {
            return organisation.getName();
        }
        return clean(requestedName);
    }

    private String cleanOrExisting(String value, String existing) {
        return hasText(value) ? value.trim() : existing;
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
