package dev.ograh.dynamicforms.submission.validator;

import dev.ograh.dynamicforms.form.entity.FieldValidation;
import dev.ograh.dynamicforms.form.entity.FormField;
import dev.ograh.dynamicforms.form.enums.FieldType;
import dev.ograh.dynamicforms.shared.exception.FormValidationException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormSubmissionValidator {

    public void validate(List<FormField> fields, Map<String, Object> data) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FormField field : fields) {
            validateField(field, data.get(field.getFieldKey()), errors);
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
    }

    private void validateField(FormField field, Object rawValue, Map<String, String> errors) {
        String value = rawValue != null ? rawValue.toString().trim() : "";
        if (isRequiredButEmpty(field, value, errors)) return;
        if (value.isEmpty()) return;
        validateConstraints(field, value, errors);
        validateOptions(field, value, errors);
    }

    private boolean isRequiredButEmpty(FormField field, String value, Map<String, String> errors) {
        if (field.isRequired() && value.isEmpty()) {
            errors.put(field.getFieldKey(), field.getLabel() + " is required");
            return true;
        }
        return false;
    }

    private void validateConstraints(FormField field, String value, Map<String, String> errors) {
        FieldValidation v = field.getValidation();
        if (v == null) return;
        validateLength(field, value, v, errors);
        validatePattern(field, value, v, errors);
        if (field.getFieldType() == FieldType.NUMBER) {
            validateNumericRange(field, value, v, errors);
        }
    }

    private void validateLength(FormField field, String value, FieldValidation v, Map<String, String> errors) {
        if (v.minLength() != null && value.length() < v.minLength()) {
            errors.put(field.getFieldKey(), "Minimum length is " + v.minLength());
        }
        if (v.maxLength() != null && value.length() > v.maxLength()) {
            errors.put(field.getFieldKey(), "Maximum length is " + v.maxLength());
        }
    }

    private void validatePattern(FormField field, String value, FieldValidation v, Map<String, String> errors) {
        if (v.pattern() != null && !value.matches(v.pattern())) {
            errors.put(field.getFieldKey(),
                    v.patternMessage() != null ? v.patternMessage() : "Invalid format");
        }
    }

    private void validateNumericRange(FormField field, String value, FieldValidation v, Map<String, String> errors) {
        try {
            double num = Double.parseDouble(value);
            if (v.min() != null && num < v.min()) {
                errors.put(field.getFieldKey(), "Minimum value is " + v.min());
            }
            if (v.max() != null && num > v.max()) {
                errors.put(field.getFieldKey(), "Maximum value is " + v.max());
            }
        } catch (NumberFormatException e) {
            errors.put(field.getFieldKey(), "Must be a number");
        }
    }

    private void validateOptions(FormField field, String value, Map<String, String> errors) {
        if (!List.of(FieldType.SELECT, FieldType.RADIO).contains(field.getFieldType())) return;
        boolean valid = field.getOptions().stream().anyMatch(opt -> opt.value().equals(value));
        if (!valid) {
            errors.put(field.getFieldKey(), "Invalid option selected");
        }
    }
}
