package za.co.ultronsport.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.AdminActionLog;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    List<AdminActionLog> findByAdminUserId(Long adminUserId);
}
