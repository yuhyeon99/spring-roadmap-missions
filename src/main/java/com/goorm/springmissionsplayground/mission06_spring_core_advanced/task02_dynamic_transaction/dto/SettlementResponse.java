package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto;

public class SettlementResponse {

    private final String orderId;
    private final int amount;
    private final String operator;
    private final String businessStatus;
    private final String message;
    private final TransactionSnapshotResponse transaction;

    public SettlementResponse(
            String orderId,
            int amount,
            String operator,
            String businessStatus,
            String message,
            TransactionSnapshotResponse transaction
    ) {
        this.orderId = orderId;
        this.amount = amount;
        this.operator = operator;
        this.businessStatus = businessStatus;
        this.message = message;
        this.transaction = transaction;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getOperator() {
        return operator;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public String getMessage() {
        return message;
    }

    public TransactionSnapshotResponse getTransaction() {
        return transaction;
    }
}
