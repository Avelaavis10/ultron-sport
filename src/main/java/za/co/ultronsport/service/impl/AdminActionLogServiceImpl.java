package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.web.dto.AdminActionLogResponse;
import za.co.ultronsport.web.dto.AdminActionLogSearchCriteria;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;
import za.co.ultronsport.web.dto.PageResponse;

@Service
public class AdminActionLogServiceImpl implements AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final UserRepository userRepository;

    public AdminActionLogServiceImpl(AdminActionLogRepository adminActionLogRepository,
                                     UserRepository userRepository) {
        this.adminActionLogRepository = adminActionLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AdminActionLog log(CreateAdminActionLogRequest request) {
        return log(request.adminUserId(), request.actionType(), request.targetType(), request.targetId(),
                request.reason(), request.details());
    }

    @Override
    @Transactional
    public AdminActionLog log(Long adminUserId, AdminActionType actionType, AdminTargetType targetType,
                              Long targetId, String reason, String details) {
        User admin = userRepository.findById(adminUserId).orElse(null);
        AdminActionLog log = AdminActionLog.create(adminUserId, admin == null ? null : admin.getEmail(),
                admin == null ? null : admin.getDisplayName(), actionType, targetType, targetId,
                blankToNull(reason), blankToNull(details));
        return adminActionLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActionLog> listForAdmin(Long adminUserId) {
        return adminActionLogRepository.findByAdminUserId(adminUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminActionLog getById(Long id) {
        return adminActionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin action log not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminActionLogResponse> search(AdminActionLogSearchCriteria criteria) {
        criteria.validate();
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(),
                Sort.by(criteria.direction(), criteria.effectiveSortBy()));
        Page<AdminActionLog> page = adminActionLogRepository.search(criteria.actionType(), criteria.targetType(),
                criteria.targetId(), criteria.adminUserId(), criteria.fromDate(), criteria.toDate(), pageRequest);
        List<AdminActionLogResponse> content = page.getContent().stream()
                .map(AdminActionLogResponse::from)
                .toList();
        return PageResponse.from(page, content, criteria.effectiveSortBy(), criteria.direction());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActionLog> listForTarget(AdminTargetType targetType, Long targetId) {
        return adminActionLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
