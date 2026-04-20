package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

public class JdbcErrorResponse {

    private final String errorCode;
    private final String message;
    private final int status;
    private final String path;
    private final String sqlState;
    private final String occurredAt;

    public JdbcErrorResponse(
            String errorCode,
            String message,
            int status,
            String path,
            String sqlState,
            String occurredAt
    ) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.path = path;
        this.sqlState = sqlState;
        this.occurredAt = occurredAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getOccurredAt() {
        return occurredAt;
    }
}
