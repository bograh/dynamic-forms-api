package dev.ograh.dynamicforms.auth.service.impl;

import dev.ograh.dynamicforms.auth.dto.*;
import dev.ograh.dynamicforms.auth.entity.User;
import dev.ograh.dynamicforms.auth.enums.Role;
import dev.ograh.dynamicforms.auth.helper.AuthHelper;
import dev.ograh.dynamicforms.auth.repository.UserRepository;
import dev.ograh.dynamicforms.auth.service.AuthService;
import dev.ograh.dynamicforms.security.JwtTokenService;
import dev.ograh.dynamicforms.shared.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthHelper authHelper;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequest request) {
        authHelper.validateEmailExists(request.email());

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        TokensDTO tokens = jwtTokenService.generateTokens(user.getEmail(), user.getRole().name());
        return toAuthResponseDTO(user, tokens);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));

        TokensDTO tokens = jwtTokenService.generateTokens(user.getEmail(), user.getRole().name());
        return toAuthResponseDTO(user, tokens);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(String refreshToken) {
        String email = jwtTokenService.extractSubject(refreshToken, true);
        String role = jwtTokenService.validateTokenAndExtractClaims(refreshToken, true)
                .get("role", String.class);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        TokensDTO tokens = jwtTokenService.generateTokens(email, role);
        return toAuthResponseDTO(user, tokens);
    }

    private AuthResponseDTO toAuthResponseDTO(User user, TokensDTO tokens) {
        return new AuthResponseDTO(
                new AuthResponse(toUserResponse(user), tokens.accessToken()),
                tokens.refreshToken()
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }
}