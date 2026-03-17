package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception.ErrorGuideNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = ErrorGuideController.class)
public class ErrorPageControllerAdvice {

    @ExceptionHandler(ErrorGuideNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ErrorGuideNotFoundException exception, HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorName", HttpStatus.NOT_FOUND.getReasonPhrase());
        model.addAttribute("alertTitle", "요청한 자원을 찾을 수 없습니다.");
        model.addAttribute("alertMessage", exception.getMessage());
        model.addAttribute("requestedId", exception.getGuideId());
        model.addAttribute("requestUri", request.getRequestURI());
        return "mission04/task10/error-page-404";
    }
}
