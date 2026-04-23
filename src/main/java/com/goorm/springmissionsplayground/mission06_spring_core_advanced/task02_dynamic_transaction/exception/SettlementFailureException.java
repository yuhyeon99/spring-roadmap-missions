package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.exception;

public class SettlementFailureException extends RuntimeException {

    public SettlementFailureException(String message) {
        super(message);
    }
}
