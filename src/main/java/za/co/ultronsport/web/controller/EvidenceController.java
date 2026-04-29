package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.service.EvidenceService;
import za.co.ultronsport.web.dto.CreateEvidenceRequest;
import za.co.ultronsport.web.dto.EvidenceResponse;
import za.co.ultronsport.web.dto.FlagEvidenceRequest;
import za.co.ultronsport.web.dto.RejectEvidenceRequest;
import za.co.ultronsport.web.dto.UpdateEvidenceRequest;
import za.co.ultronsport.web.dto.VerificationActionResponse;
import za.co.ultronsport.web.dto.VerificationRequestResponse;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceResponse create(@Valid @RequestBody CreateEvidenceRequest request, Authentication authentication) {
        // TODO: Add file upload scanning before accepting direct uploads.
        SecurityUser currentUser = currentUser(authentication);
        return EvidenceResponse.from(evidenceService.createDraftEvidence(currentUser.getId(), request));
    }

    @GetMapping("/{id}")
    public EvidenceResponse getById(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return EvidenceResponse.from(evidenceService.getEvidenceById(currentUser.getId(), currentUser.getRole(), id));
    }

    @GetMapping("/my")
    public List<EvidenceResponse> myEvidence(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return evidenceService.getMyEvidence(currentUser.getId()).stream()
                .map(EvidenceResponse::from)
                .toList();
    }

    @PatchMapping("/{id}")
    public EvidenceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEvidenceRequest request,
                                   Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return EvidenceResponse.from(evidenceService.updateEvidence(currentUser.getId(), id, request));
    }

    @PostMapping("/{id}/submit")
    public VerificationActionResponse submit(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return VerificationActionResponse.from(evidenceService.submitEvidence(currentUser.getId(), id),
                "Evidence submitted for verification.");
    }

    @GetMapping("/pending-verification")
    public List<EvidenceResponse> pendingVerification() {
        return evidenceService.getPendingVerificationEvidence().stream()
                .map(EvidenceResponse::from)
                .toList();
    }

    @PostMapping("/{id}/verify")
    public VerificationActionResponse verify(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return VerificationActionResponse.from(evidenceService.verifyEvidence(currentUser.getId(), id),
                "Evidence verified.");
    }

    @PostMapping("/{id}/reject")
    public VerificationActionResponse reject(@PathVariable Long id,
                                             @Valid @RequestBody RejectEvidenceRequest request,
                                             Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return VerificationActionResponse.from(evidenceService.rejectEvidence(currentUser.getId(), id, request),
                "Evidence rejected.");
    }

    @PostMapping("/{id}/flag")
    public VerificationActionResponse flag(@PathVariable Long id,
                                           @Valid @RequestBody FlagEvidenceRequest request,
                                           Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        // TODO: Add admin moderation queue integration for flagged evidence.
        return VerificationActionResponse.from(evidenceService.flagEvidence(currentUser.getId(), id, request),
                "Evidence flagged for moderation.");
    }

    @PostMapping("/{id}/archive")
    public VerificationActionResponse archive(@PathVariable Long id, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return VerificationActionResponse.from(evidenceService.archiveEvidence(currentUser.getId(), id),
                "Evidence archived.");
    }

    @GetMapping("/{id}/verification-history")
    public List<VerificationRequestResponse> verificationHistory(@PathVariable Long id) {
        return evidenceService.getVerificationHistory(id).stream()
                .map(VerificationRequestResponse::from)
                .toList();
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
