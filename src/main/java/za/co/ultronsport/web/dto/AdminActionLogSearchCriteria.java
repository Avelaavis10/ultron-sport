package za.co.ultronsport.web.dto;

import java.time.Instant;
import java.util.Set;
import org.springframework.data.domain.Sort;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;

public record AdminActionLogSearchCriteria(
        AdminActionType actionType,
        AdminTargetType targetType,
        Long targetId,
        Long adminUserId,
        Instant fromDate,
        Instant toDate,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;
    public static final String DEFAULT_SORT = "createdAt";
    public static final String DEFAULT_DIRECTION = "DESC";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "actionType", "targetType",
            "targetId", "adminUserId");

    public static AdminActionLogSearchCriteria from(String actionType, String targetType, Long targetId,
                                                    Long adminUserId, Instant fromDate, Instant toDate,
                                                    Integer page, Integer size, String sortBy,
                                                    String sortDirection) {
        return new AdminActionLogSearchCriteria(parseActionType(actionType), parseTargetType(targetType),
                targetId, adminUserId, fromDate, toDate, page == null ? DEFAULT_PAGE : page,
                size == null ? DEFAULT_SIZE : size, sortBy, sortDirection);
    }

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

    public void validate() {
        if (page < 0) {
            throw new InvalidStateException("Page must be zero or greater.");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidStateException("Page size must be between 1 and 50.");
        }
        if (!ALLOWED_SORT_FIELDS.contains(effectiveSortBy())) {
            throw new InvalidStateException("Invalid sort field: " + effectiveSortBy());
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidStateException("fromDate cannot be after toDate.");
        }
    }

    private static AdminActionType parseActionType(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return AdminActionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStateException("Invalid actionType: " + value);
        }
    }

    public static AdminTargetType parseTargetType(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return AdminTargetType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStateException("Invalid targetType: " + value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
