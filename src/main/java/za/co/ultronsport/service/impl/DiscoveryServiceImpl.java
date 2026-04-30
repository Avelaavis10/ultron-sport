package za.co.ultronsport.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.Achievement;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.AchievementRepository;
import za.co.ultronsport.repository.AthleteProfileRepository;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.repository.LevelPlayScoreRepository;
import za.co.ultronsport.repository.OrganisationRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.DiscoveryService;
import za.co.ultronsport.web.dto.AchievementSummaryResponse;
import za.co.ultronsport.web.dto.AthleteDiscoveryCardResponse;
import za.co.ultronsport.web.dto.AthleteDiscoveryProfileResponse;
import za.co.ultronsport.web.dto.AthleteSearchCriteria;
import za.co.ultronsport.web.dto.EvidenceDiscoveryCardResponse;
import za.co.ultronsport.web.dto.LevelPlayScoreSummaryResponse;
import za.co.ultronsport.web.dto.PageResponse;
import za.co.ultronsport.web.dto.VerificationSummaryResponse;

@Service
public class DiscoveryServiceImpl implements DiscoveryService {

    private static final Set<String> ATHLETE_SORT_FIELDS = Set.of("createdAt", "updatedAt", "sport",
            "position", "location", "profileCompletenessScore");
    private static final Set<String> EVIDENCE_SORT_FIELDS = Set.of("createdAt", "updatedAt", "eventDate",
            "sport", "position", "title", "verificationStatus");

    private final AthleteProfileRepository athleteProfileRepository;
    private final EvidenceUploadRepository evidenceUploadRepository;
    private final LevelPlayScoreRepository levelPlayScoreRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;

    public DiscoveryServiceImpl(AthleteProfileRepository athleteProfileRepository,
                                EvidenceUploadRepository evidenceUploadRepository,
                                LevelPlayScoreRepository levelPlayScoreRepository,
                                AchievementRepository achievementRepository,
                                UserRepository userRepository,
                                OrganisationRepository organisationRepository) {
        this.athleteProfileRepository = athleteProfileRepository;
        this.evidenceUploadRepository = evidenceUploadRepository;
        this.levelPlayScoreRepository = levelPlayScoreRepository;
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.organisationRepository = organisationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AthleteDiscoveryCardResponse> searchAthletes(SecurityUser currentUser,
                                                                     AthleteSearchCriteria criteria) {
        validate(criteria, ATHLETE_SORT_FIELDS);
        PageRequest pageRequest = pageRequest(criteria);
        EvidenceVisibility visibility = athleteVisibility(currentUser.getRole(), criteria);
        if (visibility.empty()) {
            return PageResponse.from(Page.empty(pageRequest), List.of(), criteria);
        }

        Page<AthleteProfile> page = athleteProfileRepository.findAll(
                athleteSpec(criteria, currentUser.getRole(), visibility), pageRequest);
        List<AthleteDiscoveryCardResponse> cards = buildAthleteCards(page.getContent());
        return PageResponse.from(page, cards, criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteDiscoveryProfileResponse getAthleteProfile(SecurityUser currentUser, Long athleteProfileId) {
        AthleteProfile profile = athleteProfileRepository.findById(athleteProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found: " + athleteProfileId));
        boolean ownProfile = currentUser.getRole() == UserRole.ATHLETE && profile.getUserId().equals(currentUser.getId());
        Set<VerificationStatus> statuses = ownProfile ? allEvidenceStatuses()
                : visibleEvidenceStatuses(currentUser.getRole());
        List<EvidenceUpload> visibleEvidence = evidenceUploadRepository
                .findByAthleteProfileIdAndVerificationStatusInOrderByCreatedAtDesc(athleteProfileId, statuses);

        if (!ownProfile && currentUser.getRole() != UserRole.ADMIN && visibleEvidence.isEmpty()) {
            throw new AccessDeniedException("This athlete profile is not discoverable for your role.");
        }

        String displayName = displayNamesByUserId(List.of(profile.getUserId())).getOrDefault(profile.getUserId(),
                "Unknown Athlete");
        String organisationName = organisationDisplayName(profile, organisationNamesByIdForProfile(profile));
        List<AchievementSummaryResponse> achievements = achievementRepository.findByAthleteProfileId(athleteProfileId)
                .stream()
                .map(AchievementSummaryResponse::from)
                .toList();
        List<EvidenceDiscoveryCardResponse> evidence = visibleEvidence.stream()
                .map(item -> EvidenceDiscoveryCardResponse.from(item, displayName))
                .toList();
        LevelPlayScore score = levelPlayScoreRepository.findByAthleteProfileId(athleteProfileId).orElse(null);
        VerificationSummaryResponse verificationSummary = verificationSummary(visibleEvidence);

        return AthleteDiscoveryProfileResponse.from(profile, displayName, organisationName, achievements, evidence,
                LevelPlayScoreSummaryResponse.from(score), verificationSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EvidenceDiscoveryCardResponse> searchEvidence(SecurityUser currentUser,
                                                                      AthleteSearchCriteria criteria) {
        validate(criteria, EVIDENCE_SORT_FIELDS);
        PageRequest pageRequest = pageRequest(criteria);
        EvidenceVisibility visibility = evidenceVisibility(currentUser.getRole(), criteria);
        if (visibility.empty()) {
            return PageResponse.from(Page.empty(pageRequest), List.of(), criteria);
        }

        Page<EvidenceUpload> page = evidenceUploadRepository.findAll(evidenceSpec(criteria, visibility), pageRequest);
        Map<Long, String> displayNames = displayNamesByAthleteProfileId(page.getContent().stream()
                .map(EvidenceUpload::getAthleteProfileId)
                .collect(Collectors.toSet()));
        List<EvidenceDiscoveryCardResponse> cards = page.getContent().stream()
                .map(evidence -> EvidenceDiscoveryCardResponse.from(evidence,
                        displayNames.getOrDefault(evidence.getAthleteProfileId(), "Unknown Athlete")))
                .toList();
        return PageResponse.from(page, cards, criteria);
    }

    private Specification<AthleteProfile> athleteSpec(AthleteSearchCriteria criteria, UserRole role,
                                                      EvidenceVisibility visibility) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addLike(criteriaBuilder, predicates, root, "sport", criteria.sport());
            addLike(criteriaBuilder, predicates, root, "position", criteria.position());
            addLike(criteriaBuilder, predicates, root, "location", criteria.location());
            if (criteria.organisationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("organisationId"), criteria.organisationId()));
            }
            addLevelPlayFilters(criteria, root, query, criteriaBuilder, predicates);
            if (visibility.requireEvidenceStatuses() != null) {
                predicates.add(existsEvidence(root, query, criteriaBuilder, visibility.requireEvidenceStatuses()));
            }
            if (visibility.excludeVerified()) {
                predicates.add(criteriaBuilder.not(existsEvidence(root, query, criteriaBuilder,
                        Set.of(VerificationStatus.VERIFIED))));
            }
            if (role != UserRole.ADMIN && visibility.requireEvidenceStatuses() == null) {
                predicates.add(existsEvidence(root, query, criteriaBuilder, visibleEvidenceStatuses(role)));
            }
            addAthleteKeyword(criteria, root, query, criteriaBuilder, predicates);
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<EvidenceUpload> evidenceSpec(AthleteSearchCriteria criteria, EvidenceVisibility visibility) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addLike(criteriaBuilder, predicates, root, "sport", criteria.sport());
            addLike(criteriaBuilder, predicates, root, "position", criteria.position());
            if (visibility.requireEvidenceStatuses() != null) {
                predicates.add(root.get("verificationStatus").in(visibility.requireEvidenceStatuses()));
            }
            if (visibility.excludeVerified()) {
                predicates.add(criteriaBuilder.notEqual(root.get("verificationStatus"), VerificationStatus.VERIFIED));
            }
            addEvidenceProfileFilters(criteria, root, query, criteriaBuilder, predicates);
            addEvidenceLevelPlayFilters(criteria, root, query, criteriaBuilder, predicates);
            addEvidenceKeyword(criteria, root, query, criteriaBuilder, predicates);
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private EvidenceVisibility athleteVisibility(UserRole role, AthleteSearchCriteria criteria) {
        return visibilityFor(role, criteria);
    }

    private EvidenceVisibility evidenceVisibility(UserRole role, AthleteSearchCriteria criteria) {
        return visibilityFor(role, criteria);
    }

    private EvidenceVisibility visibilityFor(UserRole role, AthleteSearchCriteria criteria) {
        Set<VerificationStatus> allowedStatuses = visibleEvidenceStatuses(role);
        Set<VerificationStatus> requiredStatuses = null;

        if (criteria.verificationStatus() != null) {
            if (!allowedStatuses.contains(criteria.verificationStatus())) {
                return EvidenceVisibility.emptyVisibility();
            }
            requiredStatuses = Set.of(criteria.verificationStatus());
        } else if (role != UserRole.ADMIN) {
            requiredStatuses = allowedStatuses;
        }

        boolean excludeVerified = false;
        if (Boolean.TRUE.equals(criteria.hasVerifiedEvidence())) {
            requiredStatuses = requiredStatuses == null ? Set.of(VerificationStatus.VERIFIED)
                    : intersect(requiredStatuses, Set.of(VerificationStatus.VERIFIED));
            if (requiredStatuses.isEmpty()) {
                return EvidenceVisibility.emptyVisibility();
            }
        } else if (Boolean.FALSE.equals(criteria.hasVerifiedEvidence())) {
            if (role == UserRole.SCOUT_AGENT || role == UserRole.ORGANISATION || role == UserRole.ATHLETE) {
                return EvidenceVisibility.emptyVisibility();
            }
            excludeVerified = true;
        }

        return new EvidenceVisibility(requiredStatuses, excludeVerified, false);
    }

    private Set<VerificationStatus> visibleEvidenceStatuses(UserRole role) {
        return switch (role) {
            case ADMIN -> allEvidenceStatuses();
            case COACH -> Set.of(VerificationStatus.VERIFIED, VerificationStatus.PENDING_VERIFICATION);
            case SCOUT_AGENT, ORGANISATION, ATHLETE -> Set.of(VerificationStatus.VERIFIED);
            default -> Set.of(VerificationStatus.VERIFIED);
        };
    }

    private Set<VerificationStatus> allEvidenceStatuses() {
        return EnumSet.allOf(VerificationStatus.class);
    }

    private List<AthleteDiscoveryCardResponse> buildAthleteCards(List<AthleteProfile> profiles) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        Set<Long> athleteProfileIds = profiles.stream().map(AthleteProfile::getId).collect(Collectors.toSet());
        Map<Long, String> displayNames = displayNamesByUserId(profiles.stream()
                .map(AthleteProfile::getUserId)
                .collect(Collectors.toSet()));
        Map<Long, String> organisationNames = organisationNamesById(profiles.stream()
                .map(AthleteProfile::getOrganisationId)
                .collect(Collectors.toSet()));
        Map<Long, LevelPlayScore> scores = levelPlayScoreRepository.findByAthleteProfileIdIn(athleteProfileIds)
                .stream()
                .collect(Collectors.toMap(LevelPlayScore::getAthleteProfileId, Function.identity()));
        List<EvidenceUpload> verifiedEvidence = evidenceUploadRepository
                .findByAthleteProfileIdInAndVerificationStatusInOrderByCreatedAtDesc(athleteProfileIds,
                        Set.of(VerificationStatus.VERIFIED));
        Map<Long, Long> verifiedCounts = verifiedEvidence.stream()
                .collect(Collectors.groupingBy(EvidenceUpload::getAthleteProfileId, Collectors.counting()));
        Map<Long, String> latestVerifiedTitles = new HashMap<>();
        for (EvidenceUpload evidence : verifiedEvidence) {
            latestVerifiedTitles.putIfAbsent(evidence.getAthleteProfileId(), evidence.getTitle());
        }

        return profiles.stream()
                .map(profile -> AthleteDiscoveryCardResponse.from(profile,
                        displayNames.getOrDefault(profile.getUserId(), "Unknown Athlete"),
                        organisationDisplayName(profile, organisationNames),
                        verifiedCounts.getOrDefault(profile.getId(), 0L),
                        latestVerifiedTitles.get(profile.getId()),
                        scores.get(profile.getId())))
                .toList();
    }

    private VerificationSummaryResponse verificationSummary(List<EvidenceUpload> evidence) {
        return new VerificationSummaryResponse(evidence.size(),
                countStatus(evidence, VerificationStatus.VERIFIED),
                countStatus(evidence, VerificationStatus.PENDING_VERIFICATION),
                countStatus(evidence, VerificationStatus.REJECTED),
                countStatus(evidence, VerificationStatus.FLAGGED),
                countStatus(evidence, VerificationStatus.ARCHIVED));
    }

    private long countStatus(List<EvidenceUpload> evidence, VerificationStatus status) {
        return evidence.stream().filter(item -> item.getVerificationStatus() == status).count();
    }

    private void addLike(CriteriaBuilder criteriaBuilder, List<Predicate> predicates, Root<?> root,
                         String field, String value) {
        if (hasText(value)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)),
                    "%" + value.trim().toLowerCase() + "%"));
        }
    }

    private void addLevelPlayFilters(AthleteSearchCriteria criteria, Root<AthleteProfile> root,
                                     CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
                                     List<Predicate> predicates) {
        if (criteria.minLevelPlayScore() == null && criteria.maxLevelPlayScore() == null && criteria.tier() == null) {
            return;
        }
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<LevelPlayScore> score = subquery.from(LevelPlayScore.class);
        List<Predicate> scorePredicates = new ArrayList<>();
        scorePredicates.add(criteriaBuilder.equal(score.get("athleteProfileId"), root.get("id")));
        if (criteria.minLevelPlayScore() != null) {
            scorePredicates.add(criteriaBuilder.greaterThanOrEqualTo(score.get("finalCredibilityScore"),
                    criteria.minLevelPlayScore()));
        }
        if (criteria.maxLevelPlayScore() != null) {
            scorePredicates.add(criteriaBuilder.lessThanOrEqualTo(score.get("finalCredibilityScore"),
                    criteria.maxLevelPlayScore()));
        }
        if (criteria.tier() != null) {
            scorePredicates.add(criteriaBuilder.equal(score.get("tier"), criteria.tier()));
        }
        subquery.select(score.get("athleteProfileId"))
                .where(scorePredicates.toArray(Predicate[]::new));
        predicates.add(criteriaBuilder.exists(subquery));
    }

    private void addEvidenceLevelPlayFilters(AthleteSearchCriteria criteria, Root<EvidenceUpload> root,
                                             CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
                                             List<Predicate> predicates) {
        if (criteria.minLevelPlayScore() == null && criteria.maxLevelPlayScore() == null && criteria.tier() == null) {
            return;
        }
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<LevelPlayScore> score = subquery.from(LevelPlayScore.class);
        List<Predicate> scorePredicates = new ArrayList<>();
        scorePredicates.add(criteriaBuilder.equal(score.get("athleteProfileId"), root.get("athleteProfileId")));
        if (criteria.minLevelPlayScore() != null) {
            scorePredicates.add(criteriaBuilder.greaterThanOrEqualTo(score.get("finalCredibilityScore"),
                    criteria.minLevelPlayScore()));
        }
        if (criteria.maxLevelPlayScore() != null) {
            scorePredicates.add(criteriaBuilder.lessThanOrEqualTo(score.get("finalCredibilityScore"),
                    criteria.maxLevelPlayScore()));
        }
        if (criteria.tier() != null) {
            scorePredicates.add(criteriaBuilder.equal(score.get("tier"), criteria.tier()));
        }
        subquery.select(score.get("athleteProfileId"))
                .where(scorePredicates.toArray(Predicate[]::new));
        predicates.add(criteriaBuilder.exists(subquery));
    }

    private Predicate existsEvidence(Root<AthleteProfile> root, CriteriaQuery<?> query,
                                     CriteriaBuilder criteriaBuilder, Collection<VerificationStatus> statuses) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<EvidenceUpload> evidence = subquery.from(EvidenceUpload.class);
        subquery.select(evidence.get("athleteProfileId"))
                .where(criteriaBuilder.equal(evidence.get("athleteProfileId"), root.get("id")),
                        evidence.get("verificationStatus").in(statuses));
        return criteriaBuilder.exists(subquery);
    }

    private void addAthleteKeyword(AthleteSearchCriteria criteria, Root<AthleteProfile> root, CriteriaQuery<?> query,
                                   CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        String keyword = criteria.normalizedKeyword();
        if (keyword == null) {
            return;
        }
        List<Predicate> keywordPredicates = new ArrayList<>();
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sport")), "%" + keyword + "%"));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("position")), "%" + keyword + "%"));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("schoolOrClub")), "%" + keyword + "%"));
        keywordPredicates.add(existsOrganisationKeyword(root, query, criteriaBuilder, keyword));
        keywordPredicates.add(existsUserDisplayName(root, query, criteriaBuilder, keyword));
        keywordPredicates.add(existsEvidenceKeyword(root, query, criteriaBuilder, keyword));
        predicates.add(criteriaBuilder.or(keywordPredicates.toArray(Predicate[]::new)));
    }

    private Predicate existsUserDisplayName(Root<AthleteProfile> root, CriteriaQuery<?> query,
                                            CriteriaBuilder criteriaBuilder, String keyword) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<User> user = subquery.from(User.class);
        subquery.select(user.get("id"))
                .where(criteriaBuilder.equal(user.get("id"), root.get("userId")),
                        criteriaBuilder.like(criteriaBuilder.lower(user.get("displayName")), "%" + keyword + "%"));
        return criteriaBuilder.exists(subquery);
    }

    private Predicate existsEvidenceKeyword(Root<AthleteProfile> root, CriteriaQuery<?> query,
                                            CriteriaBuilder criteriaBuilder, String keyword) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<EvidenceUpload> evidence = subquery.from(EvidenceUpload.class);
        subquery.select(evidence.get("athleteProfileId"))
                .where(criteriaBuilder.equal(evidence.get("athleteProfileId"), root.get("id")),
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(evidence.get("title")),
                                        "%" + keyword + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(evidence.get("description")),
                                        "%" + keyword + "%")));
        return criteriaBuilder.exists(subquery);
    }

    private void addEvidenceProfileFilters(AthleteSearchCriteria criteria, Root<EvidenceUpload> root,
                                           CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
                                           List<Predicate> predicates) {
        if (!hasText(criteria.location()) && criteria.organisationId() == null) {
            return;
        }
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AthleteProfile> profile = subquery.from(AthleteProfile.class);
        List<Predicate> profilePredicates = new ArrayList<>();
        profilePredicates.add(criteriaBuilder.equal(profile.get("id"), root.get("athleteProfileId")));
        if (hasText(criteria.location())) {
            profilePredicates.add(criteriaBuilder.like(criteriaBuilder.lower(profile.get("location")),
                    "%" + criteria.location().trim().toLowerCase() + "%"));
        }
        if (criteria.organisationId() != null) {
            profilePredicates.add(criteriaBuilder.equal(profile.get("organisationId"), criteria.organisationId()));
        }
        subquery.select(profile.get("id")).where(profilePredicates.toArray(Predicate[]::new));
        predicates.add(criteriaBuilder.exists(subquery));
    }

    private void addEvidenceKeyword(AthleteSearchCriteria criteria, Root<EvidenceUpload> root, CriteriaQuery<?> query,
                                    CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        String keyword = criteria.normalizedKeyword();
        if (keyword == null) {
            return;
        }
        List<Predicate> keywordPredicates = new ArrayList<>();
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword + "%"));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + keyword + "%"));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("sport")), "%" + keyword + "%"));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("position")), "%" + keyword + "%"));
        keywordPredicates.add(existsEvidenceAthleteKeyword(root, query, criteriaBuilder, keyword));
        keywordPredicates.add(existsEvidenceAthleteOrganisationKeyword(root, query, criteriaBuilder, keyword));
        predicates.add(criteriaBuilder.or(keywordPredicates.toArray(Predicate[]::new)));
    }

    private Predicate existsEvidenceAthleteKeyword(Root<EvidenceUpload> root, CriteriaQuery<?> query,
                                                   CriteriaBuilder criteriaBuilder, String keyword) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AthleteProfile> profile = subquery.from(AthleteProfile.class);
        Root<User> user = subquery.from(User.class);
        subquery.select(profile.get("id"))
                .where(criteriaBuilder.equal(profile.get("id"), root.get("athleteProfileId")),
                        criteriaBuilder.equal(user.get("id"), profile.get("userId")),
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(user.get("displayName")),
                                        "%" + keyword + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(profile.get("schoolOrClub")),
                                        "%" + keyword + "%")));
        return criteriaBuilder.exists(subquery);
    }

    private Predicate existsEvidenceAthleteOrganisationKeyword(Root<EvidenceUpload> root, CriteriaQuery<?> query,
                                                              CriteriaBuilder criteriaBuilder, String keyword) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AthleteProfile> profile = subquery.from(AthleteProfile.class);
        Root<Organisation> organisation = subquery.from(Organisation.class);
        subquery.select(profile.get("id"))
                .where(criteriaBuilder.equal(profile.get("id"), root.get("athleteProfileId")),
                        criteriaBuilder.equal(organisation.get("id"), profile.get("organisationId")),
                        criteriaBuilder.like(criteriaBuilder.lower(organisation.get("name")),
                                "%" + keyword + "%"));
        return criteriaBuilder.exists(subquery);
    }

    private Predicate existsOrganisationKeyword(Root<AthleteProfile> root, CriteriaQuery<?> query,
                                                CriteriaBuilder criteriaBuilder, String keyword) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organisation> organisation = subquery.from(Organisation.class);
        subquery.select(organisation.get("id"))
                .where(criteriaBuilder.equal(organisation.get("id"), root.get("organisationId")),
                        criteriaBuilder.like(criteriaBuilder.lower(organisation.get("name")),
                                "%" + keyword + "%"));
        return criteriaBuilder.exists(subquery);
    }

    private Map<Long, String> displayNamesByAthleteProfileId(Collection<Long> athleteProfileIds) {
        if (athleteProfileIds.isEmpty()) {
            return Map.of();
        }
        List<AthleteProfile> profiles = toList(athleteProfileRepository.findAllById(athleteProfileIds));
        Map<Long, String> displayNamesByUserId = displayNamesByUserId(profiles.stream()
                .map(AthleteProfile::getUserId)
                .collect(Collectors.toSet()));
        return profiles.stream()
                .collect(Collectors.toMap(AthleteProfile::getId,
                        profile -> displayNamesByUserId.getOrDefault(profile.getUserId(), "Unknown Athlete")));
    }

    private Map<Long, String> displayNamesByUserId(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return toList(userRepository.findAllById(userIds)).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));
    }

    private Map<Long, String> organisationNamesById(Collection<Long> organisationIds) {
        Set<Long> ids = organisationIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return toList(organisationRepository.findAllById(ids)).stream()
                .collect(Collectors.toMap(Organisation::getId, Organisation::getName));
    }

    private String organisationDisplayName(AthleteProfile profile, Map<Long, String> organisationNames) {
        if (profile.getOrganisationId() == null) {
            return profile.getSchoolOrClub();
        }
        return organisationNames.getOrDefault(profile.getOrganisationId(), profile.getSchoolOrClub());
    }

    private Map<Long, String> organisationNamesByIdForProfile(AthleteProfile profile) {
        if (profile.getOrganisationId() == null) {
            return Map.of();
        }
        return organisationNamesById(List.of(profile.getOrganisationId()));
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> items = new ArrayList<>();
        iterable.forEach(items::add);
        return items;
    }

    private PageRequest pageRequest(AthleteSearchCriteria criteria) {
        return PageRequest.of(criteria.page(), criteria.size(),
                Sort.by(criteria.direction(), criteria.effectiveSortBy()));
    }

    private void validate(AthleteSearchCriteria criteria, Set<String> allowedSortFields) {
        criteria.validate();
        if (!allowedSortFields.contains(criteria.effectiveSortBy())) {
            throw new InvalidStateException("Invalid sort field: " + criteria.effectiveSortBy());
        }
    }

    private Set<VerificationStatus> intersect(Set<VerificationStatus> left, Set<VerificationStatus> right) {
        Set<VerificationStatus> result = new HashSet<>(left);
        result.retainAll(right);
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record EvidenceVisibility(
            Set<VerificationStatus> requireEvidenceStatuses,
            boolean excludeVerified,
            boolean empty
    ) {
        static EvidenceVisibility emptyVisibility() {
            return new EvidenceVisibility(Set.of(), false, true);
        }
    }
}
