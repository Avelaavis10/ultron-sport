package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;

public record AdminActionLogResponse(
        Long id,
        Long adminUserId,
        String adminEmail,
        String adminDisplayName,
        AdminActionType actionType,
        AdminTargetType targetType,
        Long targetId,
        String reason,
        String details,
        Instant createdAt
) {
    public static AdminActionLogResponse from(AdminActionLog log) {
        return new AdminActionLogResponse(log.getId(), log.getAdminUserId(), log.getAdminEmail(),
                log.getAdminDisplayName(), log.getActionType(), log.getTargetType(), log.getTargetId(),
                log.getReason(), log.getDetails(), log.getCreatedAt());
    }
}
