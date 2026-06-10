package dev.ograh.dynamicforms.form.dto;

import dev.ograh.dynamicforms.form.enums.FormStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FormDto(
        UUID id,
        String title,
        String description,
        String slug,
        FormStatus status,
        List<FormFieldDto> fields,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
