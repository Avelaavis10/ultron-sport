package za.co.ultronsport.web.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.service.EvidenceUploadService;
import za.co.ultronsport.web.dto.CreateEvidenceUploadRequest;
import za.co.ultronsport.web.dto.EvidenceUploadResponse;

@RestController
@RequestMapping("/api/v1/evidence")
public class EvidenceUploadController {

    private final EvidenceUploadService evidenceUploadService;

    public EvidenceUploadController(EvidenceUploadService evidenceUploadService) {
        this.evidenceUploadService = evidenceUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceUploadResponse create(@Valid @RequestBody CreateEvidenceUploadRequest request) {
        // TODO: Add file upload scanning before accepting direct uploads.
        return EvidenceUploadResponse.from(evidenceUploadService.create(request));
    }

    @GetMapping("/{id}")
    public EvidenceUploadResponse getById(@PathVariable Long id) {
        return EvidenceUploadResponse.from(evidenceUploadService.getById(id));
    }

    @GetMapping("/athlete/{athleteProfileId}")
    public List<EvidenceUploadResponse> listForAthlete(@PathVariable Long athleteProfileId) {
        return evidenceUploadService.listForAthlete(athleteProfileId).stream()
                .map(EvidenceUploadResponse::from)
                .toList();
    }
}
