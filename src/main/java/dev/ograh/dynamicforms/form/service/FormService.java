package dev.ograh.dynamicforms.form.service;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
import dev.ograh.dynamicforms.form.dto.request.CreateFormRequest;
import dev.ograh.dynamicforms.form.dto.request.FormFieldRequest;
import dev.ograh.dynamicforms.form.dto.request.UpdateFormRequest;
import dev.ograh.dynamicforms.submission.dto.SubmissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FormService {
    FormSchemaDto getPublishedBySlug(String slug);
    List<FormSchemaDto> findAllPublished();
    List<FormDto> findAll();
    FormDto getById(String id);
    FormDto create(CreateFormRequest request);
    FormDto update(String id, UpdateFormRequest request);
    FormDto saveFields(String id, List<FormFieldRequest> fields);
    void publish(String id);
    Page<SubmissionDto> getSubmissions(String id, Pageable pageable);
}
