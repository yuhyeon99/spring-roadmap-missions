package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.SecurityErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthenticatedAccessException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthorizedProjectAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProxySecurityController.class)
public class ProxySecurityExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthenticatedAccessException.class)
    public SecurityErrorResponse handleUnauthenticated(
            UnauthenticatedAccessException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHENTICATED",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedProjectAccessException.class)
    public SecurityErrorResponse handleUnauthorized(
            UnauthorizedProjectAccessException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public SecurityErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
