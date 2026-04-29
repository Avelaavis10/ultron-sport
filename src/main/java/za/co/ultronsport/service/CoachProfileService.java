package za.co.ultronsport.service;

import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;

public interface CoachProfileService {
    CoachProfile create(CreateCoachProfileRequest request);

    CoachProfile getById(Long id);
}
