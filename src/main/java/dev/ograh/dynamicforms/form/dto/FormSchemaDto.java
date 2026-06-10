package dev.ograh.dynamicforms.form.dto;

import java.util.List;

public record FormSchemaDto(
        String id,
        String title,
        String description,
        String slug,
        List<FormFieldSchemaDto> fields
) {}
