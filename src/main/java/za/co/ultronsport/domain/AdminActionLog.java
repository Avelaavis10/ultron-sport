package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_action_logs")
public class AdminActionLog extends BaseEntity {

    @Column(nullable = false)
    private Long adminUserId;

    private String adminEmail;
    private String adminDisplayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 1200)
    private String reason;

    @Column(length = 2000)
    private String details;

    protected AdminActionLog() {
    }

    private AdminActionLog(Long adminUserId, String adminEmail, String adminDisplayName,
                           AdminActionType actionType, AdminTargetType targetType,
                           Long targetId, String reason, String details) {
        this.adminUserId = adminUserId;
        this.adminEmail = adminEmail;
        this.adminDisplayName = adminDisplayName;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.details = details;
    }

    public static AdminActionLog create(Long adminUserId, String adminEmail, String adminDisplayName,
                                        AdminActionType actionType, AdminTargetType targetType,
                                        Long targetId, String reason, String details) {
        return new AdminActionLog(adminUserId, adminEmail, adminDisplayName, actionType, targetType,
                targetId, reason, details);
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getAdminDisplayName() {
        return adminDisplayName;
    }

    public AdminActionType getActionType() {
        return actionType;
    }

    public AdminTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }
}
