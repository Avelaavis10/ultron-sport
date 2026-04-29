package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.AdminActionLogRepository;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.impl.AdminActionLogServiceImpl;
import za.co.ultronsport.web.dto.AdminActionLogSearchCriteria;
import za.co.ultronsport.web.dto.CreateAdminActionLogRequest;

@ExtendWith(MockitoExtension.class)
class AdminActionLogServiceImplTest {

    @Mock
    private AdminActionLogRepository adminActionLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminActionLogServiceImpl adminActionLogService;

    @Test
    void logCreatesAdminActionLog() {
        CreateAdminActionLogRequest request = new CreateAdminActionLogRequest(1L, AdminActionType.EVIDENCE_FLAGGED,
                AdminTargetType.EVIDENCE, 5L, "Suspicious metadata", "Evidence looks duplicated");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(adminActionLogRepository.save(any(AdminActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminActionLog log = adminActionLogService.log(request);

        assertThat(log.getActionType()).isEqualTo(AdminActionType.EVIDENCE_FLAGGED);
        assertThat(log.getTargetType()).isEqualTo(AdminTargetType.EVIDENCE);
        assertThat(log.getAdminEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void searchFiltersByActionType() {
        AdminActionLogSearchCriteria criteria = AdminActionLogSearchCriteria.from("EVIDENCE_FLAGGED", null,
                null, null, null, null, 0, 20, null, null);
        when(adminActionLogRepository.search(eq(AdminActionType.EVIDENCE_FLAGGED), eq(null), eq(null),
                eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(Page.empty());

        adminActionLogService.search(criteria);

        verify(adminActionLogRepository).search(eq(AdminActionType.EVIDENCE_FLAGGED), eq(null), eq(null),
                eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void searchFiltersByTargetTypeAndTargetId() {
        AdminActionLogSearchCriteria criteria = AdminActionLogSearchCriteria.from(null, "EVIDENCE",
                5L, null, null, null, 0, 20, null, null);
        when(adminActionLogRepository.search(eq(null), eq(AdminTargetType.EVIDENCE), eq(5L),
                eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(Page.empty());

        adminActionLogService.search(criteria);

        verify(adminActionLogRepository).search(eq(null), eq(AdminTargetType.EVIDENCE), eq(5L),
                eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void pageSizeAbove50IsRejected() {
        AdminActionLogSearchCriteria criteria = AdminActionLogSearchCriteria.from(null, null,
                null, null, null, null, 0, 51, null, null);

        assertThatThrownBy(() -> adminActionLogService.search(criteria))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("Page size must be between 1 and 50.");
        verify(adminActionLogRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void serviceDoesNotExposeEditOrDeleteMethods() {
        assertThat(AdminActionLogService.class.getMethods())
                .noneMatch(method -> method.getName().toLowerCase().contains("delete"))
                .noneMatch(method -> method.getName().toLowerCase().contains("update"));
    }

    private User adminUser() {
        User user = User.create("Admin User", "admin@example.com", null, "hashed", UserRole.ADMIN);
        user.activate();
        return user;
    }
}
