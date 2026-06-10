package dev.ograh.dynamicforms.form.helper;

import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.repository.FormRepository;
import dev.ograh.dynamicforms.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormHelper {

    private final FormRepository formRepository;

    public Form findById(String id) {
        return formRepository.findById(parseId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + id));
    }

    public UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid form id: " + id);
        }
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
