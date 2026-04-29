package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.service.impl.AdminActionLogServiceImpl;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;

@ExtendWith(MockitoExtension.class)
class AdminActionLogServiceImplTest {

    @Mock
    private AdminActionLogRepository adminActionLogRepository;

    @InjectMocks
    private AdminActionLogServiceImpl adminActionLogService;

    @Test
    void logCreatesAdminActionLog() {
        CreateAdminActionLogRequest request = new CreateAdminActionLogRequest(1L, AdminActionType.EVIDENCE_FLAGGED,
                "EvidenceUpload", 5L, "Suspicious metadata");
        when(adminActionLogRepository.save(any(AdminActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminActionLog log = adminActionLogService.log(request);

        assertThat(log.getActionType()).isEqualTo(AdminActionType.EVIDENCE_FLAGGED);
    }
}
