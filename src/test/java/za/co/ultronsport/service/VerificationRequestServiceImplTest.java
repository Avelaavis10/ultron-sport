package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.ultronsport.common.error.InvalidStateException;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceType;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;
import za.co.ultronsport.repository.VerificationRequestRepository;
import za.co.ultronsport.service.impl.VerificationRequestServiceImpl;
import za.co.ultronsport.web.dto.CreateVerificationRequest;

@ExtendWith(MockitoExtension.class)
class VerificationRequestServiceImplTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private EvidenceUploadService evidenceUploadService;

    @InjectMocks
    private VerificationRequestServiceImpl verificationRequestService;

    @Test
    void createStoresPendingVerificationRequest() {
        CreateVerificationRequest request = new CreateVerificationRequest(10L, 1L, 2L);
        EvidenceUpload evidence = EvidenceUpload.create(1L, 2L, EvidenceType.VIDEO, "Football", "Striker",
                "Goal", java.time.LocalDate.now(), EvidenceContext.MATCH, "https://file", null, null);
        when(evidenceUploadService.getById(10L)).thenReturn(evidence);
        when(verificationRequestRepository.save(any(VerificationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationRequest saved = verificationRequestService.create(request);

        assertThat(saved.getStatus()).isEqualTo(VerificationStatus.PENDING_VERIFICATION);
    }

    @Test
    void approveChangesStatusAndEvidenceStatus() {
        VerificationRequest request = VerificationRequest.create(10L, 1L, 2L);
        when(verificationRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(verificationRequestRepository.save(request)).thenReturn(request);

        VerificationRequest approved = verificationRequestService.approve(5L, "Valid clip");

        assertThat(approved.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        verify(evidenceUploadService).markVerified(10L);
    }

    @Test
    void approveAlreadyDecidedRequestThrowsInvalidState() {
        VerificationRequest request = VerificationRequest.create(10L, 1L, 2L);
        request.approve("first");
        when(verificationRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> verificationRequestService.approve(5L, "second"))
                .isInstanceOf(InvalidStateException.class);
    }
}
