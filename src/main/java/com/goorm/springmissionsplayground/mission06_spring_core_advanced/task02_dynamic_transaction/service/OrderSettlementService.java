package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service;

public interface OrderSettlementService {

    SettlementResult settle(String orderId, int amount, String operator);

    SettlementResult settleWithFailure(String orderId, int amount, String operator);
}
