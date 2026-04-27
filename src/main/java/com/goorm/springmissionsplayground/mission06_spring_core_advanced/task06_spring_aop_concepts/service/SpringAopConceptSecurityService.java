package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.annotation.RequireRole;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopSecurityRequest;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopUserRole;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SpringAopConceptSecurityService {

    @RequireRole(value = AopUserRole.USER, action = "배포 계획 조회")
    public AopSecuredOperationResult viewDeploymentPlan(String planId, AopSecurityRequest request) {
        return new AopSecuredOperationResult(
                planId,
                "배포 계획 조회",
                request.getOperatorId(),
                request.getRole(),
                AopUserRole.USER,
                "배포 계획 " + planId + "의 단계, 담당자, 롤백 절차를 조회했습니다."
        );
    }

    @RequireRole(value = AopUserRole.ADMIN, action = "운영 배포 승인")
    public AopSecuredOperationResult approveProductionDeployment(String planId, AopSecurityRequest request) {
        return new AopSecuredOperationResult(
                planId,
                "운영 배포 승인",
                request.getOperatorId(),
                request.getRole(),
                AopUserRole.ADMIN,
                "운영 배포 계획 " + planId + "의 최종 승인 검토를 완료했습니다."
        );
    }

    public List<String> conceptKeywords() {
        return List.of("Aspect", "Advice", "Pointcut", "JoinPoint", "Proxy");
    }
}
