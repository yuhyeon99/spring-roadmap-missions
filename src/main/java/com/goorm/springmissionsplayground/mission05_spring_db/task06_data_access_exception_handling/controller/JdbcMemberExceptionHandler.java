package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcErrorResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcDataAccessException;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcMemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = JdbcMemberController.class)
public class JdbcMemberExceptionHandler {

    @ExceptionHandler(JdbcDataAccessException.class)
    public ResponseEntity<JdbcErrorResponse> handleJdbcDataAccessException(
            JdbcDataAccessException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(buildErrorResponse(
                        exception.getErrorCode(),
                        exception.getUserMessage(),
                        exception.getStatus(),
                        request.getRequestURI(),
                        exception.getSqlState()
                ));
    }

    @ExceptionHandler(JdbcMemberNotFoundException.class)
    public ResponseEntity<JdbcErrorResponse> handleNotFoundException(
            JdbcMemberNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        "MEMBER_NOT_FOUND",
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND,
                        request.getRequestURI(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JdbcErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        FieldError firstError = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String message = firstError != null
                ? firstError.getDefaultMessage()
                : "입력값을 다시 확인해주세요.";

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        "INVALID_REQUEST",
                        message,
                        HttpStatus.BAD_REQUEST,
                        request.getRequestURI(),
                        null
                ));
    }

    private JdbcErrorResponse buildErrorResponse(
            String errorCode,
            String message,
            HttpStatus status,
            String path,
            String sqlState
    ) {
        return new JdbcErrorResponse(
                errorCode,
                message,
                status.value(),
                path,
                sqlState,
                OffsetDateTime.now().toString()
        );
    }
}
