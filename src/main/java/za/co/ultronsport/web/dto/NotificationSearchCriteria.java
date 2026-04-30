package za.co.ultronsport.web.dto;

import java.util.Set;
import org.springframework.data.domain.Sort;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.NotificationStatus;

public record NotificationSearchCriteria(
        NotificationStatus status,
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

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "readAt", "type", "status",
            "targetType", "targetId");

    public static NotificationSearchCriteria from(String status, Integer page, Integer size, String sortBy,
                                                  String sortDirection) {
        return new NotificationSearchCriteria(parseStatus(status), page == null ? DEFAULT_PAGE : page,
                size == null ? DEFAULT_SIZE : size, sortBy, sortDirection);
    }

    public NotificationSearchCriteria unreadOnly() {
        return new NotificationSearchCriteria(NotificationStatus.UNREAD, page, size, sortBy, sortDirection);
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
    }

    private static NotificationStatus parseStatus(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return NotificationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStateException("Invalid notification status: " + value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
