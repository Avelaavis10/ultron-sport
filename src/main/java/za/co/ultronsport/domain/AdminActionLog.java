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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionType actionType;

    @Column(nullable = false)
    private String targetEntityType;

    @Column(nullable = false)
    private Long targetEntityId;

    @Column(length = 1200)
    private String reason;

    protected AdminActionLog() {
    }

    private AdminActionLog(Long adminUserId, AdminActionType actionType, String targetEntityType,
                           Long targetEntityId, String reason) {
        this.adminUserId = adminUserId;
        this.actionType = actionType;
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
        this.reason = reason;
    }

    public static AdminActionLog create(Long adminUserId, AdminActionType actionType, String targetEntityType,
                                        Long targetEntityId, String reason) {
        return new AdminActionLog(adminUserId, actionType, targetEntityType, targetEntityId, reason);
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public AdminActionType getActionType() {
        return actionType;
    }

    public String getTargetEntityType() {
        return targetEntityType;
    }

    public Long getTargetEntityId() {
        return targetEntityId;
    }

    public String getReason() {
        return reason;
    }
}
