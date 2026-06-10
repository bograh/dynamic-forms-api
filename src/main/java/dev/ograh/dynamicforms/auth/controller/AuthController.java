package dev.ograh.dynamicforms.auth.controller;

import dev.ograh.dynamicforms.auth.dto.AuthResponse;
import dev.ograh.dynamicforms.auth.dto.AuthResponseDTO;
import dev.ograh.dynamicforms.auth.dto.LoginRequest;
import dev.ograh.dynamicforms.auth.dto.RegisterRequest;
import dev.ograh.dynamicforms.auth.service.AuthService;
import dev.ograh.dynamicforms.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletResponse response) {
        AuthResponseDTO dto = authService.register(request);
        setRefreshTokenCookie(dto.refreshToken(), response);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto.authResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        AuthResponseDTO dto = authService.login(request);
        setRefreshTokenCookie(dto.refreshToken(), response);
        return ResponseEntity.ok(dto.authResponse());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        AuthResponseDTO dto = authService.refreshToken(refreshToken);
        setRefreshTokenCookie(dto.refreshToken(), response);
        return ResponseEntity.ok(dto.authResponse());
    }

    private void setRefreshTokenCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh-token")
                .maxAge(jwtProperties.getRefreshTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}