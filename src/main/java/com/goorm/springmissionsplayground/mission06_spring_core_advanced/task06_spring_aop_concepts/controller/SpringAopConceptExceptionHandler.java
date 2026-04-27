package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.dto.AopConceptErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.exception.AopAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SpringAopConceptController.class)
public class SpringAopConceptExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AopAccessDeniedException.class)
    public AopConceptErrorResponse handleAccessDenied(
            AopAccessDeniedException exception,
            HttpServletRequest request
    ) {
        return new AopConceptErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public AopConceptErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new AopConceptErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
