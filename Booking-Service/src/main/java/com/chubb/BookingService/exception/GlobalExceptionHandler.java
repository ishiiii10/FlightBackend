package com.chubb.BookingService.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Booking not found
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 2. Booking already cancelled
    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyCancelled(
            BookingAlreadyCancelledException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 3. Validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    // 4. Downstream (Feign) errors – propagate underlying status where possible
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeign(
            FeignException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return buildError(
                status,
                ex.contentUTF8(),
                request.getRequestURI()
        );
    }

    // 5. Access / business rule violations inside Booking-Service
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 6. Fallback (last resort)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                request.getRequestURI()
        );
    }

    // Shared builder
    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String message,
            String path
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.name())
                        .message(message)
                        .path(path)
                        .build());
    }
}