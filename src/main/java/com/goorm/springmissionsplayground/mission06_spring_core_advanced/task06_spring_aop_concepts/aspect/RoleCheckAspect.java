package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.annotation.RequireRole;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopSecurityRequest;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.exception.AopAccessDeniedException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service.AopSecuredOperationResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support.AopAccessAuditStore;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(RoleCheckAspect.class);

    private final AopAccessAuditStore aopAccessAuditStore;

    public RoleCheckAspect(AopAccessAuditStore aopAccessAuditStore) {
        this.aopAccessAuditStore = aopAccessAuditStore;
    }

    @Before("@annotation(requireRole)")
    public void verifyRole(JoinPoint joinPoint, RequireRole requireRole) {
        AopSecurityRequest request = extractRequest(joinPoint);
        String methodLabel = joinPoint.getSignature().toShortString();

        aopAccessAuditStore.add("BEFORE", "포인트컷 일치: " + methodLabel);
        aopAccessAuditStore.add(
                "BEFORE",
                "권한 검사 시작 - action=" + requireRole.action()
                        + ", currentRole=" + request.getRole()
                        + ", requiredRole=" + requireRole.value()
        );

        if (!request.getRole().hasAtLeast(requireRole.value())) {
            String message = requireRole.action() + " 작업에는 " + requireRole.value() + " 권한이 필요합니다.";
            aopAccessAuditStore.add("DENIED", message);
            log.info("[TASK06-AOP][DENIED] {} operator={} role={}", requireRole.action(), request.getOperatorId(), request.getRole());
            throw new AopAccessDeniedException(message);
        }

        aopAccessAuditStore.add("GRANTED", request.getOperatorId() + " 사용자의 접근이 허용되었습니다.");
        log.info("[TASK06-AOP][GRANTED] {} operator={} role={}", requireRole.action(), request.getOperatorId(), request.getRole());
    }

    @AfterReturning(
            value = "@annotation(requireRole)",
            returning = "result"
    )
    public void writeAuditTrail(RequireRole requireRole, AopSecuredOperationResult result) {
        String auditMessage = "성공 감사 로그 기록 - action=" + requireRole.action()
                + ", operator=" + result.getOperatorId()
                + ", planId=" + result.getPlanId();
        aopAccessAuditStore.add("AFTER_RETURNING", auditMessage);
        log.info("[TASK06-AOP][AFTER_RETURNING] {}", auditMessage);
    }

    private AopSecurityRequest extractRequest(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof AopSecurityRequest request) {
                return request;
            }
        }
        throw new IllegalArgumentException("AOP 보안 검사에는 AopSecurityRequest 인자가 필요합니다.");
    }
}
