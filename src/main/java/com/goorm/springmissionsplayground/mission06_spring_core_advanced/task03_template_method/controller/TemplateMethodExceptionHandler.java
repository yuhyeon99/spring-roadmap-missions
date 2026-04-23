package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateMethodErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TemplateMethodController.class)
public class TemplateMethodExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public TemplateMethodErrorResponse handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new TemplateMethodErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
