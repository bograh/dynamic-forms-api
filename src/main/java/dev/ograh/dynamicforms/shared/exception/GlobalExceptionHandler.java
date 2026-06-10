package dev.ograh.dynamicforms.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResponse(e, req, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(e, req, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(JwtAuthenticationException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse(e, req, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerException(Exception e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse(e, req, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ErrorResponse buildErrorResponse(Exception ex, HttpServletRequest req, HttpStatus status) {
        return new ErrorResponse(
                ex.getMessage(),
                status.getReasonPhrase(),
                status.value(),
                LocalDateTime.now().toString(),
                req.getRequestURI()
        );
    }
}
