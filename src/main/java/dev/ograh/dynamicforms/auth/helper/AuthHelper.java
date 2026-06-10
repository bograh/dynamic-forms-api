package dev.ograh.dynamicforms.auth.helper;

import dev.ograh.dynamicforms.auth.repository.UserRepository;
import dev.ograh.dynamicforms.shared.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepository userRepository;

    public void validateEmailExists(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("User with email " + email + " already exists");
        }
    }
}