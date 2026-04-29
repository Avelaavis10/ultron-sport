package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceType;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.EvidenceUploadRepository;
import za.co.ultronsport.service.impl.EvidenceUploadServiceImpl;
import za.co.ultronsport.web.dto.CreateEvidenceUploadRequest;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadServiceImplTest {

    @Mock
    private EvidenceUploadRepository evidenceUploadRepository;

    @InjectMocks
    private EvidenceUploadServiceImpl evidenceUploadService;

    @Test
    void createStoresStructuredEvidenceMetadata() {
        CreateEvidenceUploadRequest request = new CreateEvidenceUploadRequest(1L, 2L, EvidenceType.EXTERNAL_LINK,
                "Football", "Striker", "Goal highlight", LocalDate.now(), EvidenceContext.MATCH,
                null, "https://example.com/video", "Cup final");
        when(evidenceUploadRepository.save(any(EvidenceUpload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceUpload evidence = evidenceUploadService.create(request);

        assertThat(evidence.getSport()).isEqualTo("Football");
        assertThat(evidence.getAiAnalysisStatus()).isNotNull();
    }

    @Test
    void markVerifiedChangesEvidenceStatus() {
        EvidenceUpload evidence = EvidenceUpload.create(1L, 2L, EvidenceType.VIDEO, "Football", "Striker",
                "Shot", LocalDate.now(), EvidenceContext.TRAINING, "https://file", null, null);
        when(evidenceUploadRepository.findById(9L)).thenReturn(Optional.of(evidence));
        when(evidenceUploadRepository.save(evidence)).thenReturn(evidence);

        EvidenceUpload updated = evidenceUploadService.markVerified(9L);

        assertThat(updated.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }
}
