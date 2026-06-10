package dev.ograh.dynamicforms.form.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateFormRequest(
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        String description,

        String slug
) {}
