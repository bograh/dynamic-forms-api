package dev.ograh.dynamicforms.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildError(e, req, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(e, req, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(JwtAuthenticationException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(e, req, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, duplicate) -> existing
                ));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new ErrorResponse("Validation failed", HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(), now(), req.getRequestURI(), fields)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest req) {
        Map<String, String> fields = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        cv -> cv.getPropertyPath().toString(),
                        cv -> cv.getMessage(),
                        (existing, duplicate) -> existing
                ));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new ErrorResponse("Validation failed", HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(), now(), req.getRequestURI(), fields)
        );
    }

    @ExceptionHandler(FormValidationException.class)
    public ResponseEntity<ErrorResponse> handleFormValidationException(FormValidationException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new ErrorResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(), now(), req.getRequestURI(), e.getErrors())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest req) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.contains("unique constraint") || message != null && message.contains("duplicate key")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ErrorResponse.of("A record with the same value already exists",
                            HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.value(), now(), req.getRequestURI())
            );
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of("Data integrity violation", HttpStatus.CONFLICT.getReasonPhrase(),
                        HttpStatus.CONFLICT.value(), now(), req.getRequestURI())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerException(Exception e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(e, req, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ErrorResponse buildError(Exception ex, HttpServletRequest req, HttpStatus status) {
        return ErrorResponse.of(ex.getMessage(), status.getReasonPhrase(), status.value(), now(), req.getRequestURI());
    }

    private String now() {
        return LocalDateTime.now().toString();
    }
}
