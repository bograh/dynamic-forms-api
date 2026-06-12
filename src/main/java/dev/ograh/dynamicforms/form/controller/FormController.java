package dev.ograh.dynamicforms.form.controller;

import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
import dev.ograh.dynamicforms.form.service.FormService;
import dev.ograh.dynamicforms.submission.entity.FormSubmission;
import dev.ograh.dynamicforms.submission.dto.SubmissionResponseDto;
import dev.ograh.dynamicforms.submission.service.FormSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;
    private final FormSubmissionService submissionService;

    @GetMapping
    public ResponseEntity<List<FormSchemaDto>> listPublished() {
        return ResponseEntity.ok(formService.findAllPublished());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<FormSchemaDto> getForm(@PathVariable String slug) {
        return ResponseEntity.ok(formService.getPublishedBySlug(slug));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResponseDto> submit(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> data,
            Authentication authentication) {
        FormSubmission submission = submissionService.submit(id, data, authentication.getName());
        return ResponseEntity.status(201)
                .body(new SubmissionResponseDto(submission.getId(), "Submission received"));
    }
}
