package dev.ograh.dynamicforms.form.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFormRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        String description,

        @NotBlank(message = "Slug is required")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
        String slug
) {}
