package za.co.ultronsport.service;

import za.co.ultronsport.web.dto.HealthReadinessResponse;

public interface HealthService {
    HealthReadinessResponse readiness();
}
