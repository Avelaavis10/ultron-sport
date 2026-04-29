package za.co.ultronsport.web.dto;

import org.springframework.data.domain.Sort;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.LevelPlayTier;
import za.co.ultronsport.domain.VerificationStatus;

public record AthleteSearchCriteria(
        String sport,
        String position,
        String location,
        Long organisationId,
        VerificationStatus verificationStatus,
        Integer minLevelPlayScore,
        Integer maxLevelPlayScore,
        LevelPlayTier tier,
        Boolean hasVerifiedEvidence,
        String keyword,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;
    public static final String DEFAULT_SORT = "updatedAt";
    public static final String DEFAULT_DIRECTION = "DESC";

    public Sort.Direction direction() {
        try {
            return Sort.Direction.fromString(sortDirection == null ? DEFAULT_DIRECTION : sortDirection);
        } catch (IllegalArgumentException ex) {
            throw new InvalidStateException("Invalid sort direction: " + sortDirection);
        }
    }

    public String effectiveSortBy() {
        return hasText(sortBy) ? sortBy : DEFAULT_SORT;
    }

    public String normalizedKeyword() {
        return hasText(keyword) ? keyword.trim().toLowerCase() : null;
    }

    public void validate() {
        if (page < 0) {
            throw new InvalidStateException("Page must be zero or greater.");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidStateException("Page size must be between 1 and 50.");
        }
        if (minLevelPlayScore != null && maxLevelPlayScore != null && minLevelPlayScore > maxLevelPlayScore) {
            throw new InvalidStateException("Minimum LevelPlay score cannot exceed maximum LevelPlay score.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
