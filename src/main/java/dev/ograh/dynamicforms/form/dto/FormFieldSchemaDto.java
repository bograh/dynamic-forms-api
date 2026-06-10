package dev.ograh.dynamicforms.form.dto;

import dev.ograh.dynamicforms.form.entity.FieldOption;
import dev.ograh.dynamicforms.form.entity.FieldValidation;
import dev.ograh.dynamicforms.form.enums.FieldType;

import java.util.List;

public record FormFieldSchemaDto(
        String fieldKey,
        String label,
        String helpText,
        String placeholder,
        String defaultValue,
        int fieldOrder,
        boolean required,
        FieldType fieldType,
        List<FieldOption> options,
        FieldValidation validation
) {}
