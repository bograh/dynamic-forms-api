package dev.ograh.dynamicforms.form.dto;

import dev.ograh.dynamicforms.form.entity.FieldOption;
import dev.ograh.dynamicforms.form.entity.FieldValidation;
import dev.ograh.dynamicforms.form.enums.FieldType;

import java.util.List;
import java.util.UUID;

public record FormFieldDto(
        UUID id,
        String label,
        String fieldKey,
        String helpText,
        String placeholder,
        String defaultValue,
        int fieldOrder,
        boolean required,
        FieldType fieldType,
        List<FieldOption> options,
        FieldValidation validation
) {}
