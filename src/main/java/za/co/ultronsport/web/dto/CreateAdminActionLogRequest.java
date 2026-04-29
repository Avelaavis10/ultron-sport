package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import za.co.ultronsport.domain.AdminActionType;

public record CreateAdminActionLogRequest(
        @NotNull Long adminUserId,
        @NotNull AdminActionType actionType,
        @NotBlank String targetEntityType,
        @NotNull Long targetEntityId,
        String reason
) {
}
