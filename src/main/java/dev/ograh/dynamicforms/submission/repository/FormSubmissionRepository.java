package dev.ograh.dynamicforms.submission.repository;

import dev.ograh.dynamicforms.submission.entity.FormSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {
    Page<FormSubmission> findByFormId(UUID formId, Pageable pageable);
}
