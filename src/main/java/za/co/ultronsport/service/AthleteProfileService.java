package za.co.ultronsport.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;
import za.co.ultronsport.web.dto.LinkAthleteOrganisationRequest;
import za.co.ultronsport.web.dto.UpdateAthleteProfileRequest;

public interface AthleteProfileService {
    AthleteProfile create(Long currentUserId, CreateAthleteProfileRequest request);

    AthleteProfile getMyProfile(Long currentUserId);

    AthleteProfile updateMyProfile(Long currentUserId, UpdateAthleteProfileRequest request);

    AthleteProfile linkOrganisation(Long currentUserId, LinkAthleteOrganisationRequest request);

    AthleteProfile getById(Long id);

    AthleteProfile getById(Long currentUserId, UserRole currentUserRole, Long id);

    Page<AthleteProfile> listAll(Pageable pageable);

    List<AthleteProfile> search(String sport, String location, String position);
}
