package dev.ograh.dynamicforms.submission.service;

import dev.ograh.dynamicforms.submission.entity.FormSubmission;

import java.util.Map;
import java.util.UUID;

public interface FormSubmissionService {
    FormSubmission submit(UUID formId, Map<String, Object> responseData, String userId);
}
