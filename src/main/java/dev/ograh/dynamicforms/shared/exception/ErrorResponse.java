package dev.ograh.dynamicforms.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        String error,
        int status,
        String timestamp,
        String path,
        Map<String, String> fields
) {
    public static ErrorResponse of(String message, String error, int status, String timestamp, String path) {
        return new ErrorResponse(message, error, status, timestamp, path, null);
    }
}
