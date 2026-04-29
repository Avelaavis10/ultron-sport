package za.co.ultronsport.service;

import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.web.dto.AthleteDiscoveryCardResponse;
import za.co.ultronsport.web.dto.AthleteDiscoveryProfileResponse;
import za.co.ultronsport.web.dto.AthleteSearchCriteria;
import za.co.ultronsport.web.dto.EvidenceDiscoveryCardResponse;
import za.co.ultronsport.web.dto.PageResponse;

public interface DiscoveryService {
    PageResponse<AthleteDiscoveryCardResponse> searchAthletes(SecurityUser currentUser,
                                                              AthleteSearchCriteria criteria);

    AthleteDiscoveryProfileResponse getAthleteProfile(SecurityUser currentUser, Long athleteProfileId);

    PageResponse<EvidenceDiscoveryCardResponse> searchEvidence(SecurityUser currentUser,
                                                               AthleteSearchCriteria criteria);
}
