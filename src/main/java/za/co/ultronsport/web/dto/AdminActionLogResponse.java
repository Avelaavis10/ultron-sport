package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;

public record AdminActionLogResponse(
        Long id,
        Long adminUserId,
        AdminActionType actionType,
        String targetEntityType,
        Long targetEntityId,
        String reason
) {
    public static AdminActionLogResponse from(AdminActionLog log) {
        return new AdminActionLogResponse(log.getId(), log.getAdminUserId(), log.getActionType(),
                log.getTargetEntityType(), log.getTargetEntityId(), log.getReason());
    }
}
