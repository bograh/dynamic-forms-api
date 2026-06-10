package dev.ograh.dynamicforms.shared.exception;

public class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
