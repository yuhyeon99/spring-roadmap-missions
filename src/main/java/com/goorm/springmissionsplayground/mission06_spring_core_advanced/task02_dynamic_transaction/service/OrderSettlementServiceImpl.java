package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.exception.SettlementFailureException;

public class OrderSettlementServiceImpl implements OrderSettlementService {

    @Override
    public SettlementResult settle(String orderId, int amount, String operator) {
        System.out.println("[BUSINESS] 주문 정산 시작: orderId=%s amount=%d operator=%s"
                .formatted(orderId, amount, operator));
        System.out.println("[BUSINESS] 정산 데이터 검증 완료");
        System.out.println("[BUSINESS] 원장 반영 완료");

        return new SettlementResult(
                orderId,
                amount,
                operator,
                "SETTLEMENT_COMPLETED",
                "주문 정산이 정상적으로 커밋되었습니다."
        );
    }

    @Override
    public SettlementResult settleWithFailure(String orderId, int amount, String operator) {
        System.out.println("[BUSINESS] 주문 정산 시작: orderId=%s amount=%d operator=%s"
                .formatted(orderId, amount, operator));
        System.out.println("[BUSINESS] 정산 데이터 검증 완료");
        System.out.println("[BUSINESS] 외부 원장 반영 중 오류 감지");

        throw new SettlementFailureException("외부 원장 반영 실패로 트랜잭션을 롤백합니다.");
    }
}
