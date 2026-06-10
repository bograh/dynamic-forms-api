package dev.ograh.dynamicforms.form.helper;

import dev.ograh.dynamicforms.form.dto.FormDto;
import dev.ograh.dynamicforms.form.dto.FormFieldDto;
import dev.ograh.dynamicforms.form.dto.FormFieldSchemaDto;
import dev.ograh.dynamicforms.form.dto.FormSchemaDto;
import dev.ograh.dynamicforms.form.dto.request.FormFieldRequest;
import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.entity.FormField;
import dev.ograh.dynamicforms.submission.entity.FormSubmission;
import dev.ograh.dynamicforms.submission.dto.SubmissionDto;
import org.springframework.stereotype.Component;

@Component
public class FormMapper {

    public FormDto toFormDto(Form form) {
        return new FormDto(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getSlug(),
                form.getStatus(),
                form.getFields().stream().map(this::toFormFieldDto).toList(),
                form.getCreatedBy(),
                form.getCreatedAt(),
                form.getUpdatedAt()
        );
    }

    public FormSchemaDto toFormSchemaDto(Form form) {
        return new FormSchemaDto(
                form.getId().toString(),
                form.getTitle(),
                form.getDescription(),
                form.getSlug(),
                form.getFields().stream().map(this::toFormFieldSchemaDto).toList()
        );
    }

    public FormField toFormFieldEntity(FormFieldRequest req, Form form) {
        FormField field = new FormField();
        field.setForm(form);
        field.setLabel(req.label());
        field.setFieldKey(req.fieldKey());
        field.setHelpText(req.helpText());
        field.setPlaceholder(req.placeholder());
        field.setDefaultValue(req.defaultValue());
        field.setFieldOrder(req.fieldOrder());
        field.setRequired(req.required());
        field.setFieldType(req.fieldType());
        field.setOptions(req.options());
        field.setValidation(req.validation());
        return field;
    }

    public SubmissionDto toSubmissionDto(FormSubmission submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getSubmittedBy(),
                submission.getSubmittedAt(),
                submission.getData()
        );
    }

    private FormFieldDto toFormFieldDto(FormField field) {
        return new FormFieldDto(
                field.getId(),
                field.getLabel(),
                field.getFieldKey(),
                field.getHelpText(),
                field.getPlaceholder(),
                field.getDefaultValue(),
                field.getFieldOrder(),
                field.isRequired(),
                field.getFieldType(),
                field.getOptions(),
                field.getValidation()
        );
    }

    private FormFieldSchemaDto toFormFieldSchemaDto(FormField field) {
        return new FormFieldSchemaDto(
                field.getFieldKey(),
                field.getLabel(),
                field.getHelpText(),
                field.getPlaceholder(),
                field.getDefaultValue(),
                field.getFieldOrder(),
                field.isRequired(),
                field.getFieldType(),
                field.getOptions(),
                field.getValidation()
        );
    }
}
