package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;

@Service
public class AdminActionLogServiceImpl implements AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;

    public AdminActionLogServiceImpl(AdminActionLogRepository adminActionLogRepository) {
        this.adminActionLogRepository = adminActionLogRepository;
    }

    @Override
    public AdminActionLog log(CreateAdminActionLogRequest request) {
        AdminActionLog log = AdminActionLog.create(request.adminUserId(), request.actionType(),
                request.targetEntityType(), request.targetEntityId(), request.reason());
        return adminActionLogRepository.save(log);
    }

    @Override
    public List<AdminActionLog> listForAdmin(Long adminUserId) {
        return adminActionLogRepository.findByAdminUserId(adminUserId);
    }
}
