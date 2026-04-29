package za.co.ultronsport.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    List<AdminActionLog> findByAdminUserId(Long adminUserId);

    List<AdminActionLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(AdminTargetType targetType, Long targetId);

    @Query("""
            select log
            from AdminActionLog log
            where (:actionType is null or log.actionType = :actionType)
              and (:targetType is null or log.targetType = :targetType)
              and (:targetId is null or log.targetId = :targetId)
              and (:adminUserId is null or log.adminUserId = :adminUserId)
              and (:fromDate is null or log.createdAt >= :fromDate)
              and (:toDate is null or log.createdAt <= :toDate)
            """)
    Page<AdminActionLog> search(@Param("actionType") AdminActionType actionType,
                                @Param("targetType") AdminTargetType targetType,
                                @Param("targetId") Long targetId,
                                @Param("adminUserId") Long adminUserId,
                                @Param("fromDate") Instant fromDate,
                                @Param("toDate") Instant toDate,
                                Pageable pageable);
}
