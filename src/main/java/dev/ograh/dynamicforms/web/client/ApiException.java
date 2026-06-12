package dev.ograh.dynamicforms.web.client;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final int statusCode;
    private final Map<String, String> errors;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errors = Map.of();
    }

    public ApiException(int statusCode, String message, Map<String, String> errors) {
        super(message);
        this.statusCode = statusCode;
        this.errors = errors != null ? errors : Map.of();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
