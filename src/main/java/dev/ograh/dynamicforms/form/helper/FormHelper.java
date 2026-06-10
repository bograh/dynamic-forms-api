package dev.ograh.dynamicforms.form.helper;

import dev.ograh.dynamicforms.form.entity.Form;
import dev.ograh.dynamicforms.form.repository.FormRepository;
import dev.ograh.dynamicforms.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FormHelper {

    private static final String SUFFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

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

    public String generateSlug(String title) {
        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");
        String suffix = RANDOM.ints(6, 0, SUFFIX_CHARS.length())
                .mapToObj(i -> String.valueOf(SUFFIX_CHARS.charAt(i)))
                .collect(java.util.stream.Collectors.joining());
        return base + "-" + suffix;
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
