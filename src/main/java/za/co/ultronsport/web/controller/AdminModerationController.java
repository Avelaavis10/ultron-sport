package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.service.AdminActionLogService;
import za.co.ultronsport.service.AdminModerationService;
import za.co.ultronsport.web.dto.AdminActionLogResponse;
import za.co.ultronsport.web.dto.AdminActionLogSearchCriteria;
import za.co.ultronsport.web.dto.CreateModerationNoteRequest;
import za.co.ultronsport.web.dto.EvidenceResponse;
import za.co.ultronsport.web.dto.ModerationSummaryResponse;
import za.co.ultronsport.web.dto.PageResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminModerationController {

    private final AdminActionLogService adminActionLogService;
    private final AdminModerationService adminModerationService;

    public AdminModerationController(AdminActionLogService adminActionLogService,
                                     AdminModerationService adminModerationService) {
        this.adminActionLogService = adminActionLogService;
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/audit-logs")
    public PageResponse<AdminActionLogResponse> searchAuditLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long adminUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        AdminActionLogSearchCriteria criteria = AdminActionLogSearchCriteria.from(actionType, targetType, targetId,
                adminUserId, fromDate, toDate, page, size, sortBy, sortDirection);
        return adminActionLogService.search(criteria);
    }

    @GetMapping("/audit-logs/{id}")
    public AdminActionLogResponse getAuditLog(@PathVariable Long id) {
        return AdminActionLogResponse.from(adminActionLogService.getById(id));
    }

    @GetMapping("/audit-logs/target/{targetType}/{targetId}")
    public List<AdminActionLogResponse> getAuditLogsForTarget(@PathVariable String targetType,
                                                              @PathVariable Long targetId) {
        AdminTargetType parsedTargetType = AdminActionLogSearchCriteria.parseTargetType(targetType);
        return adminActionLogService.listForTarget(parsedTargetType, targetId).stream()
                .map(AdminActionLogResponse::from)
                .toList();
    }

    @GetMapping("/moderation/evidence/flagged")
    public List<EvidenceResponse> flaggedEvidence() {
        return adminModerationService.getFlaggedEvidence().stream()
                .map(EvidenceResponse::from)
                .toList();
    }

    @GetMapping("/moderation/evidence/archived")
    public List<EvidenceResponse> archivedEvidence() {
        return adminModerationService.getArchivedEvidence().stream()
                .map(EvidenceResponse::from)
                .toList();
    }

    @PostMapping("/moderation/evidence/{evidenceId}/note")
    public AdminActionLogResponse createModerationNote(@PathVariable Long evidenceId,
                                                       @Valid @RequestBody CreateModerationNoteRequest request,
                                                       Authentication authentication) {
        SecurityUser currentUser = (SecurityUser) authentication.getPrincipal();
        return AdminActionLogResponse.from(adminModerationService.createEvidenceModerationNote(currentUser.getId(),
                evidenceId, request));
    }

    @GetMapping("/moderation/summary")
    public ModerationSummaryResponse moderationSummary() {
        return adminModerationService.getSummary();
    }
}
