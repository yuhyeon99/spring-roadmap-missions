package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception;

import org.springframework.http.HttpStatus;

public class JdbcDataAccessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String userMessage;
    private final String sqlState;

    public JdbcDataAccessException(
            HttpStatus status,
            String errorCode,
            String userMessage,
            String sqlState,
            Throwable cause
    ) {
        super(userMessage, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.sqlState = sqlState;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getSqlState() {
        return sqlState;
    }
}
