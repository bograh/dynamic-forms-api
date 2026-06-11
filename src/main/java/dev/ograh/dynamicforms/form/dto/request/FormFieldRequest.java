package dev.ograh.dynamicforms.form.dto.request;

import dev.ograh.dynamicforms.form.entity.FieldOption;
import dev.ograh.dynamicforms.form.entity.FieldValidation;
import dev.ograh.dynamicforms.form.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FormFieldRequest(
        @NotBlank(message = "Label is required")
        String label,

        @NotBlank(message = "Field key is required")
        String fieldKey,

        String helpText,
        String placeholder,
        String defaultValue,
        int fieldOrder,
        boolean required,

        @NotNull(message = "Field type is required")
        FieldType fieldType,

        List<FieldOption> options,
        FieldValidation validation
) {}