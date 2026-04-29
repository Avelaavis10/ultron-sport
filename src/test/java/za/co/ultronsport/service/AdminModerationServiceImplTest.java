package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.AdminActionLog;
import za.co.ultronsport.domain.AdminActionType;
import za.co.ultronsport.domain.AdminTargetType;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.service.impl.AdminModerationServiceImpl;
import za.co.ultronsport.web.dto.CreateModerationNoteRequest;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceImplTest {

    @Mock
    private EvidenceUploadRepository evidenceUploadRepository;

    @Mock
    private AdminActionLogService adminActionLogService;

    @InjectMocks
    private AdminModerationServiceImpl adminModerationService;

    @Test
    void moderationNoteCreatesAuditLog() {
        EvidenceUpload evidence = EvidenceUpload.createDraft(1L, 11L, "Goal highlight", "Cup final goal",
                "Football", "Striker", "Match highlight", EvidenceContext.MATCH, LocalDate.now(),
                null, "https://video.example/highlight");
        AdminActionLog log = AdminActionLog.create(99L, "admin@example.com", "Admin User",
                AdminActionType.MODERATION_NOTE_CREATED, AdminTargetType.EVIDENCE, 7L,
                "Review note", "Needs follow-up");
        when(evidenceUploadRepository.findById(7L)).thenReturn(Optional.of(evidence));
        when(adminActionLogService.log(eq(99L), eq(AdminActionType.MODERATION_NOTE_CREATED),
                eq(AdminTargetType.EVIDENCE), eq(7L), eq("Review note"), eq("Needs follow-up"))).thenReturn(log);

        AdminActionLog created = adminModerationService.createEvidenceModerationNote(99L, 7L,
                new CreateModerationNoteRequest("Review note", "Needs follow-up"));

        assertThat(created.getActionType()).isEqualTo(AdminActionType.MODERATION_NOTE_CREATED);
        verify(adminActionLogService).log(eq(99L), eq(AdminActionType.MODERATION_NOTE_CREATED),
                eq(AdminTargetType.EVIDENCE), eq(7L), eq("Review note"), eq("Needs follow-up"));
    }
}
