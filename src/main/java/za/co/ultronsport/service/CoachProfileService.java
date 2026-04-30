package za.co.ultronsport.service;

import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;
import za.co.ultronsport.web.dto.UpdateCoachProfileRequest;

public interface CoachProfileService {
    CoachProfile create(Long currentUserId, CreateCoachProfileRequest request);

    CoachProfile getMyProfile(Long currentUserId);

    CoachProfile updateMyProfile(Long currentUserId, UpdateCoachProfileRequest request);

    CoachProfile getById(Long id);

    CoachProfile getById(Long currentUserId, UserRole currentUserRole, Long id);
}
