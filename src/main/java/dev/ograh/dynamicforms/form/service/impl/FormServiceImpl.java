package dev.ograh.dynamicforms.form.service.impl;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
import dev.ograh.dynamicforms.form.dto.request.CreateFormRequest;
import dev.ograh.dynamicforms.form.dto.request.FormFieldRequest;
import dev.ograh.dynamicforms.form.dto.request.UpdateFormRequest;
import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.enums.FormStatus;
import dev.ograh.dynamicforms.form.helper.FormHelper;
import dev.ograh.dynamicforms.form.helper.FormMapper;
import dev.ograh.dynamicforms.form.repository.FormRepository;
import dev.ograh.dynamicforms.form.service.FormService;
import dev.ograh.dynamicforms.shared.exception.ResourceNotFoundException;
import dev.ograh.dynamicforms.submission.dto.SubmissionDto;
import dev.ograh.dynamicforms.submission.repository.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormMapper formMapper;
    private final FormHelper formHelper;

    @Override
    @Transactional(readOnly = true)
    public FormSchemaDto getPublishedBySlug(String slug) {
        Form form = formRepository.findBySlugAndStatus(slug, FormStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + slug));
        return formMapper.toFormSchemaDto(form);
    }

    @Override
    @Transactional
    public FormDto create(CreateFormRequest request) {
        Form form = new Form();
        form.setTitle(request.title());
        form.setDescription(request.description());
        form.setSlug(formHelper.generateSlug(request.title()));
        form.setStatus(FormStatus.DRAFT);
        form.setCreatedBy(formHelper.currentUsername());
        return formMapper.toFormDto(formRepository.save(form));
    }

    @Override
    @Transactional
    public FormDto update(String id, UpdateFormRequest request) {
        Form form = formHelper.findById(id);
        if (request.title() != null) form.setTitle(request.title());
        if (request.description() != null) form.setDescription(request.description());
        if (request.slug() != null) form.setSlug(request.slug());
        return formMapper.toFormDto(formRepository.save(form));
    }

    @Override
    @Transactional
    public FormDto saveFields(String id, List<FormFieldRequest> fieldRequests) {
        Form form = formHelper.findById(id);
        form.getFields().clear();
        form.getFields().addAll(
                fieldRequests.stream().map(req -> formMapper.toFormFieldEntity(req, form)).toList()
        );
        return formMapper.toFormDto(formRepository.save(form));
    }

    @Override
    @Transactional
    public void publish(String id) {
        Form form = formHelper.findById(id);
        form.setStatus(FormStatus.PUBLISHED);
        formRepository.save(form);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionDto> getSubmissions(String id, Pageable pageable) {
        return submissionRepository.findByFormId(formHelper.parseId(id), pageable)
                .map(formMapper::toSubmissionDto);
    }
}
