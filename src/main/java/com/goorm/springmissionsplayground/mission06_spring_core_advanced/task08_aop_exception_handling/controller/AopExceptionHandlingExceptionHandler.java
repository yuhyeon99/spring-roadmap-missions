package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AopExceptionHandlingController.class)
public class AopExceptionHandlingExceptionHandler {

    private final ExceptionAlertStore exceptionAlertStore;

    public AopExceptionHandlingExceptionHandler(ExceptionAlertStore exceptionAlertStore) {
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IncidentRecoveryException.class)
    public AopExceptionErrorResponse handleIncidentRecoveryFailure(
            IncidentRecoveryException exception,
            HttpServletRequest request
    ) {
        return new AopExceptionErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                exception.getMessage(),
                request.getRequestURI(),
                exceptionAlertStore.getEntries().size(),
                exceptionAlertStore.getLatestEntry().orElse(null)
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public AopExceptionErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new AopExceptionErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI(),
                exceptionAlertStore.getEntries().size(),
                exceptionAlertStore.getLatestEntry().orElse(null)
        );
    }
}
