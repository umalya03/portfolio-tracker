package com.example.portfolio.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiError(Instant.now(), 400, "Validation failed", fieldErrors));
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ApiError> handleInsufficientShares(InsufficientSharesException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError(Instant.now(), 400, ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400,
                "Invalid request body. Check the JSON format, type (BUY or SELL) "
                        + "and date (yyyy-MM-dd).", Map.of()));
    }
}