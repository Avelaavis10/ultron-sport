package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.web.dto.AdminActionLogResponse;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;

@RestController
@RequestMapping("/api/v1/admin/actions")
public class AdminModerationController {

    private final AdminActionLogService adminActionLogService;

    public AdminModerationController(AdminActionLogService adminActionLogService) {
        this.adminActionLogService = adminActionLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminActionLogResponse logAction(@Valid @RequestBody CreateAdminActionLogRequest request) {
        // TODO: Require ADMIN role and expand audit logging before production.
        // TODO: Connect admin moderation actions to moderation queues and notification workflows.
        return AdminActionLogResponse.from(adminActionLogService.log(request));
    }

    @GetMapping("/admin/{adminUserId}")
    public List<AdminActionLogResponse> listForAdmin(@PathVariable Long adminUserId) {
        return adminActionLogService.listForAdmin(adminUserId).stream()
                .map(AdminActionLogResponse::from)
                .toList();
    }
}
