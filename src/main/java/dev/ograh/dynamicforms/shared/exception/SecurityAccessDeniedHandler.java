package dev.ograh.dynamicforms.shared.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ErrorResponse errorResponse = new ErrorResponse(
                "Access Denied",
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now().toString(),
                request.getRequestURI()
        );

        response.sendError(HttpStatus.FORBIDDEN.value(), errorResponse.toString());
    }
}
