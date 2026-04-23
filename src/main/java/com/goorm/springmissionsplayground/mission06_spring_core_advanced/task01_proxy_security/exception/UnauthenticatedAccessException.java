package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception;

public class UnauthenticatedAccessException extends RuntimeException {

    public UnauthenticatedAccessException(String message) {
        super(message);
    }
}
