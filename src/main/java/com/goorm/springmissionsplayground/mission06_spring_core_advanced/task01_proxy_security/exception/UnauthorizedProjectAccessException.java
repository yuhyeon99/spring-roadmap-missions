package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception;

public class UnauthorizedProjectAccessException extends RuntimeException {

    public UnauthorizedProjectAccessException(String message) {
        super(message);
    }
}
