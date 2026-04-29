package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.VerificationRequestService;
import za.co.ultronsport.web.dto.CreateVerificationRequest;
import za.co.ultronsport.web.dto.VerificationDecisionRequest;
import za.co.ultronsport.web.dto.VerificationRequestResponse;

@RestController
@RequestMapping("/api/v1/verification-requests")
public class VerificationRequestController {

    private final VerificationRequestService verificationRequestService;

    public VerificationRequestController(VerificationRequestService verificationRequestService) {
        this.verificationRequestService = verificationRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationRequestResponse create(@Valid @RequestBody CreateVerificationRequest request) {
        return VerificationRequestResponse.from(verificationRequestService.create(request));
    }

    @GetMapping("/{id}")
    public VerificationRequestResponse getById(@PathVariable Long id) {
        return VerificationRequestResponse.from(verificationRequestService.getById(id));
    }

    @PostMapping("/{id}/approve")
    public VerificationRequestResponse approve(@PathVariable Long id,
                                               @RequestBody VerificationDecisionRequest request) {
        return VerificationRequestResponse.from(verificationRequestService.approve(id, request.comments()));
    }

    @PostMapping("/{id}/reject")
    public VerificationRequestResponse reject(@PathVariable Long id,
                                              @RequestBody VerificationDecisionRequest request) {
        return VerificationRequestResponse.from(verificationRequestService.reject(id, request.comments()));
    }

    @PostMapping("/{id}/flag")
    public VerificationRequestResponse flag(@PathVariable Long id,
                                            @RequestBody VerificationDecisionRequest request) {
        // TODO: Add audit logging and admin moderation queue integration for flagged evidence.
        return VerificationRequestResponse.from(verificationRequestService.flag(id, request.comments()));
    }
}
