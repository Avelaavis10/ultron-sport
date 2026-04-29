package za.co.ultronsport.web.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sortBy,
        String sortDirection
) {
    public static <T> PageResponse<T> from(Page<?> page, List<T> content, AthleteSearchCriteria criteria) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), criteria.effectiveSortBy(), criteria.direction().name());
    }
}
