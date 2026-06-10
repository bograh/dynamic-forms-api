package dev.ograh.dynamicforms.shared.exception;

import java.util.Map;

public class FormValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public FormValidationException(Map<String, String> errors) {
        super("Form validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
