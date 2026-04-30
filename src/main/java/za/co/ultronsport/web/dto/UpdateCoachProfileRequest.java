package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCoachProfileRequest(
        @Size(max = 120) String certificationReference,
        @Positive Long organisationId,
        @Size(max = 160) String organisationName,
        @Size(max = 80) String sport,
        @Size(max = 1000) String qualificationSummary,
        @Min(0) Integer yearsExperience
) {
}
