package dev.ograh.dynamicforms.form.controller;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.dto.request.CreateFormRequest;
import dev.ograh.dynamicforms.form.dto.request.FormFieldRequest;
import dev.ograh.dynamicforms.form.dto.request.UpdateFormRequest;
import dev.ograh.dynamicforms.form.service.FormService;
import dev.ograh.dynamicforms.submission.dto.SubmissionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/forms")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FormAdminController {

    private final FormService formService;

    @PostMapping
    public ResponseEntity<FormDto> createForm(@RequestBody @Valid CreateFormRequest req) {
        return ResponseEntity.status(201).body(formService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormDto> updateForm(@PathVariable String id,
                                              @RequestBody @Valid UpdateFormRequest req) {
        return ResponseEntity.ok(formService.update(id, req));
    }

    @PutMapping("/{id}/fields")
    public ResponseEntity<FormDto> saveFields(@PathVariable String id,
                                              @RequestBody List<FormFieldRequest> fields) {
        return ResponseEntity.ok(formService.saveFields(id, fields));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable String id) {
        formService.publish(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<Page<SubmissionDto>> getSubmissions(
            @PathVariable String id, Pageable pageable) {
        return ResponseEntity.ok(formService.getSubmissions(id, pageable));
    }
}
