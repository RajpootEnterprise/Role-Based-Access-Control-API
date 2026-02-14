package com.rbaciam.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rbaciam.dto.ErrorResponseDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class ApplicationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

    @ExceptionHandler(AuthenticationExceptionFailed.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationExceptionFailed(AuthenticationExceptionFailed ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Authentication failed: {}", ex.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("Authentication Failed", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Bad request: {}", ex.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("Bad Request", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateException(DuplicateException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Duplicate resource: {}", ex.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("Duplicate Resource", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalServerException(InternalServerException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Internal server error: {}", ex.getMessage(), ex);
            ErrorResponseDTO error = new ErrorResponseDTO("Internal Server Error", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(NotFoundException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Resource not found: {}", ex.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("Resource Not Found", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Unauthorized access: {}", ex.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("Unauthorized", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            logger.error("CONTROLLER_LOG | Validation failed: {}", errors);
            ErrorResponseDTO error = new ErrorResponseDTO("Validation Failed", errors.toString());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } finally {
            MDC.clear();
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.error("CONTROLLER_LOG | Unexpected error: {}", ex.getMessage(), ex);
            ErrorResponseDTO error = new ErrorResponseDTO("Internal Server Error", "An unexpected error occurred");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            MDC.clear();
        }
    }
}