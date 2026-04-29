package za.co.ultronsport.service.impl;

import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.repository.CoachProfileRepository;
import za.co.ultronsport.service.CoachProfileService;
import za.co.ultronsport.web.dto.CreateCoachProfileRequest;

@Service
public class CoachProfileServiceImpl implements CoachProfileService {

    private final CoachProfileRepository coachProfileRepository;

    public CoachProfileServiceImpl(CoachProfileRepository coachProfileRepository) {
        this.coachProfileRepository = coachProfileRepository;
    }

    @Override
    public CoachProfile create(CreateCoachProfileRequest request) {
        CoachProfile profile = CoachProfile.create(request.userId(), request.certificationReference(),
                request.organisationName(), request.sport());
        return coachProfileRepository.save(profile);
    }

    @Override
    public CoachProfile getById(Long id) {
        return coachProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found: " + id));
    }
}
