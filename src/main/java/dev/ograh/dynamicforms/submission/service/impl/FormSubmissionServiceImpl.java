package dev.ograh.dynamicforms.submission.service.impl;

import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.enums.FormStatus;
import dev.ograh.dynamicforms.form.repository.FormRepository;
import dev.ograh.dynamicforms.shared.exception.ResourceNotFoundException;
import dev.ograh.dynamicforms.submission.entity.FormSubmission;
import dev.ograh.dynamicforms.submission.repository.FormSubmissionRepository;
import dev.ograh.dynamicforms.submission.service.FormSubmissionService;
import dev.ograh.dynamicforms.submission.validator.FormSubmissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormRepository formRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormSubmissionValidator validator;

    @Override
    public FormSubmission submit(UUID formId, Map<String, Object> responseData, String userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found"));

        if (form.getStatus() != FormStatus.PUBLISHED) {
            throw new IllegalStateException("Form is not available for submission");
        }

        validator.validate(form.getFields(), responseData);

        FormSubmission submission = new FormSubmission();
        submission.setForm(form);
        submission.setSubmittedBy(userId);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setData(responseData);

        return submissionRepository.save(submission);
    }
}
