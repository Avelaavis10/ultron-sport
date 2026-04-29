package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;

public record CreateAdminActionLogRequest(
        @NotNull Long adminUserId,
        @NotNull AdminActionType actionType,
        @NotNull AdminTargetType targetType,
        @NotNull Long targetId,
        String reason,
        @NotBlank String details
) {
}
