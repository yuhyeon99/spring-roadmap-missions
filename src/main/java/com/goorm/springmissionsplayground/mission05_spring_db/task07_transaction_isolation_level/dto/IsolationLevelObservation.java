package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.dto;

public record IsolationLevelObservation(String isolationLevel, int firstQuantity, int secondQuantity) {

    public boolean observedSameValue() {
        return firstQuantity == secondQuantity;
    }
}
