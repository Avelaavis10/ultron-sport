package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;
import za.co.ultronsport.web.dto.AdminActionLogResponse;
import za.co.ultronsport.web.dto.AdminActionLogSearchCriteria;
import za.co.ultronsport.web.dto.PageResponse;

public interface AdminActionLogService {
    AdminActionLog log(CreateAdminActionLogRequest request);

    AdminActionLog log(Long adminUserId, AdminActionType actionType, AdminTargetType targetType,
                       Long targetId, String reason, String details);

    List<AdminActionLog> listForAdmin(Long adminUserId);

    AdminActionLog getById(Long id);

    PageResponse<AdminActionLogResponse> search(AdminActionLogSearchCriteria criteria);

    List<AdminActionLog> listForTarget(AdminTargetType targetType, Long targetId);
}
