package dev.ograh.dynamicforms.auth.service;

import dev.ograh.dynamicforms.auth.dto.AuthResponseDTO;
import dev.ograh.dynamicforms.auth.dto.LoginRequest;
import dev.ograh.dynamicforms.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponseDTO register(RegisterRequest request);
    AuthResponseDTO login(LoginRequest request);
    AuthResponseDTO refreshToken(String refreshToken);
}