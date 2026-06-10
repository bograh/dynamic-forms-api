package dev.ograh.dynamicforms.form.entity;

public record FieldValidation(
        Integer minLength,
        Integer maxLength,
        Double min,
        Double max,
        String pattern,
        String patternMessage
) {}
