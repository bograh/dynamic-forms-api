package dev.ograh.dynamicforms.shared.exception;

public record ErrorResponse(
        String message,
        String error,
        int status,
        String timestamp,
        String path
) {}
