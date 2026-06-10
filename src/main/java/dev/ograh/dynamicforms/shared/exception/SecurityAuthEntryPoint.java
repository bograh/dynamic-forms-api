package dev.ograh.dynamicforms.shared.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException, ServletException {

        ErrorResponse errorResponse = ErrorResponse.of(
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now().toString(),
                request.getRequestURI()
        );

        response.sendError(HttpStatus.UNAUTHORIZED.value(), errorResponse.toString());
    }
}
