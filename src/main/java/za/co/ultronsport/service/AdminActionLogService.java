package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;

public interface AdminActionLogService {
    AdminActionLog log(CreateAdminActionLogRequest request);

    List<AdminActionLog> listForAdmin(Long adminUserId);
}
