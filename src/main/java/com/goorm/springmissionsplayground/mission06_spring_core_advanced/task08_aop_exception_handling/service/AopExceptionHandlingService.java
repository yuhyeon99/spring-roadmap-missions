package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation.NotifyOnException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AopExceptionHandlingService {

    @NotifyOnException(
            value = "incident-recovery",
            alertTarget = "slack://ops-critical-alert"
    )
    public IncidentRecoveryResult recoverIncident(String incidentId, String operatorId, boolean triggerFailure) {
        validateIncidentId(incidentId);
        validateOperatorId(operatorId);

        if (triggerFailure) {
            throw new IncidentRecoveryException(
                    incidentId + " 장애 복구 승인 중 외부 알림 연동이 실패했습니다."
            );
        }

        return new IncidentRecoveryResult(
                incidentId,
                operatorId,
                "RECOVERY_COMPLETED",
                List.of(
                        "장애 원인 분석 리포트를 조회했습니다.",
                        "복구 스크립트 실행 조건을 점검했습니다.",
                        "운영자 승인 후 복구 작업을 완료했습니다."
                )
        );
    }

    public String healthCheck() {
        return "task08-aop-exception-handling-ok";
    }

    private void validateIncidentId(String incidentId) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("incidentId는 비어 있을 수 없습니다.");
        }
    }

    private void validateOperatorId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("operatorId는 비어 있을 수 없습니다.");
        }
    }
}
