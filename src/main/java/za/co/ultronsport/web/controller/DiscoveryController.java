package za.co.ultronsport.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.service.DiscoveryService;
import za.co.ultronsport.web.dto.AthleteDiscoveryCardResponse;
import za.co.ultronsport.web.dto.AthleteDiscoveryProfileResponse;
import za.co.ultronsport.web.dto.AthleteSearchCriteria;
import za.co.ultronsport.web.dto.EvidenceDiscoveryCardResponse;
import za.co.ultronsport.web.dto.PageResponse;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/athletes")
    public PageResponse<AthleteDiscoveryCardResponse> searchAthletes(Authentication authentication,
                                                                     @RequestParam(required = false) String sport,
                                                                     @RequestParam(required = false) String position,
                                                                     @RequestParam(required = false) String location,
                                                                     @RequestParam(required = false) Long organisationId,
                                                                     @RequestParam(required = false)
                                                                     String verificationStatus,
                                                                     @RequestParam(required = false)
                                                                     Integer minLevelPlayScore,
                                                                     @RequestParam(required = false)
                                                                     Integer maxLevelPlayScore,
                                                                     @RequestParam(required = false) String tier,
                                                                     @RequestParam(required = false)
                                                                     Boolean hasVerifiedEvidence,
                                                                     @RequestParam(required = false) String keyword,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     @RequestParam(defaultValue = "updatedAt")
                                                                     String sortBy,
                                                                     @RequestParam(defaultValue = "DESC")
                                                                     String sortDirection) {
        // TODO: Move high-volume discovery to search infrastructure when MVP traffic justifies it.
        return discoveryService.searchAthletes(currentUser(authentication), criteria(sport, position, location,
                organisationId, verificationStatus, minLevelPlayScore, maxLevelPlayScore, tier, hasVerifiedEvidence,
                keyword, page, size, sortBy, sortDirection));
    }

    @GetMapping("/athletes/{athleteProfileId}")
    public AthleteDiscoveryProfileResponse getAthleteProfile(Authentication authentication,
                                                            @PathVariable Long athleteProfileId) {
        return discoveryService.getAthleteProfile(currentUser(authentication), athleteProfileId);
    }

    @GetMapping("/evidence")
    public PageResponse<EvidenceDiscoveryCardResponse> searchEvidence(Authentication authentication,
                                                                     @RequestParam(required = false) String sport,
                                                                     @RequestParam(required = false) String position,
                                                                     @RequestParam(required = false) String location,
                                                                     @RequestParam(required = false) Long organisationId,
                                                                     @RequestParam(required = false)
                                                                     String verificationStatus,
                                                                     @RequestParam(required = false)
                                                                     Integer minLevelPlayScore,
                                                                     @RequestParam(required = false)
                                                                     Integer maxLevelPlayScore,
                                                                     @RequestParam(required = false) String tier,
                                                                     @RequestParam(required = false)
                                                                     Boolean hasVerifiedEvidence,
                                                                     @RequestParam(required = false) String keyword,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size,
                                                                     @RequestParam(defaultValue = "updatedAt")
                                                                     String sortBy,
                                                                     @RequestParam(defaultValue = "DESC")
                                                                     String sortDirection) {
        // TODO: Add caching and recommendation signals after the relational MVP search is proven.
        return discoveryService.searchEvidence(currentUser(authentication), criteria(sport, position, location,
                organisationId, verificationStatus, minLevelPlayScore, maxLevelPlayScore, tier, hasVerifiedEvidence,
                keyword, page, size, sortBy, sortDirection));
    }

    private AthleteSearchCriteria criteria(String sport, String position, String location, Long organisationId,
                                           String verificationStatus, Integer minLevelPlayScore,
                                           Integer maxLevelPlayScore, String tier, Boolean hasVerifiedEvidence,
                                           String keyword, int page, int size, String sortBy, String sortDirection) {
        return new AthleteSearchCriteria(sport, position, location, organisationId,
                parseEnum(VerificationStatus.class, verificationStatus, "verificationStatus"),
                minLevelPlayScore, maxLevelPlayScore, parseEnum(LevelPlayTier.class, tier, "tier"),
                hasVerifiedEvidence, keyword, page, size, sortBy, sortDirection);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStateException("Invalid " + fieldName + ": " + value);
        }
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
