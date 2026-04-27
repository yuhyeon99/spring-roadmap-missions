package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopSecurityRequest;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopUserRole;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.dto.AopConceptSummaryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.dto.AopSecurityOperationResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service.SpringAopConceptSecurityService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support.AopAccessAuditStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task06/aop-concepts")
public class SpringAopConceptController {

    private final SpringAopConceptSecurityService springAopConceptSecurityService;
    private final AopAccessAuditStore aopAccessAuditStore;

    public SpringAopConceptController(
            SpringAopConceptSecurityService springAopConceptSecurityService,
            AopAccessAuditStore aopAccessAuditStore
    ) {
        this.springAopConceptSecurityService = springAopConceptSecurityService;
        this.aopAccessAuditStore = aopAccessAuditStore;
    }

    @GetMapping("/summary")
    public AopConceptSummaryResponse summary() {
        return new AopConceptSummaryResponse(
                "스프링 AOP는 프록시를 통해 공통 관심사를 비즈니스 코드 밖으로 분리합니다.",
                springAopConceptSecurityService.conceptKeywords(),
                List.of(
                        "JoinPoint는 메서드 실행 지점을 의미합니다.",
                        "Pointcut은 어떤 메서드에 Advice를 적용할지 고르는 규칙입니다.",
                        "Spring AOP는 프록시 기반이라 self-invocation은 기본적으로 가로채지 못합니다."
                )
        );
    }

    @GetMapping("/deployment-plans/{planId}")
    public AopSecurityOperationResponse viewDeploymentPlan(
            @PathVariable String planId,
            @RequestParam(defaultValue = "release-user") String operatorId,
            @RequestParam(defaultValue = "USER") String role
    ) {
        aopAccessAuditStore.reset();
        return AopSecurityOperationResponse.from(
                springAopConceptSecurityService.viewDeploymentPlan(planId, createRequest(operatorId, role)),
                aopAccessAuditStore.getEntries()
        );
    }

    @GetMapping("/deployment-plans/{planId}/approval")
    public AopSecurityOperationResponse approveProductionDeployment(
            @PathVariable String planId,
            @RequestParam(defaultValue = "release-admin") String operatorId,
            @RequestParam(defaultValue = "ADMIN") String role
    ) {
        aopAccessAuditStore.reset();
        return AopSecurityOperationResponse.from(
                springAopConceptSecurityService.approveProductionDeployment(planId, createRequest(operatorId, role)),
                aopAccessAuditStore.getEntries()
        );
    }

    private AopSecurityRequest createRequest(String operatorId, String role) {
        return new AopSecurityRequest(operatorId, AopUserRole.from(role));
    }
}
