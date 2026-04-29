package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.web.dto.CreateAthleteProfileRequest;

public interface AthleteProfileService {
    AthleteProfile create(CreateAthleteProfileRequest request);

    AthleteProfile getById(Long id);

    List<AthleteProfile> search(String sport, String location, String position);
}
