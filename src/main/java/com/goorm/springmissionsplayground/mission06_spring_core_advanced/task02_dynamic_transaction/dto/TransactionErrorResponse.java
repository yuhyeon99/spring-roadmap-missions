package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto;

public class TransactionErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final TransactionSnapshotResponse transaction;

    public TransactionErrorResponse(
            int status,
            String error,
            String message,
            String path,
            TransactionSnapshotResponse transaction
    ) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.transaction = transaction;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public TransactionSnapshotResponse getTransaction() {
        return transaction;
    }
}
