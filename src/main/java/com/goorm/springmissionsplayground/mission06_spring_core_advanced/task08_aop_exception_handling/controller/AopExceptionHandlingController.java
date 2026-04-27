package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionAlertHistoryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionRecoveryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionSummaryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.AopExceptionHandlingService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task08/aop-exception-handling")
public class AopExceptionHandlingController {

    private final AopExceptionHandlingService aopExceptionHandlingService;
    private final ExceptionAlertStore exceptionAlertStore;

    public AopExceptionHandlingController(
            AopExceptionHandlingService aopExceptionHandlingService,
            ExceptionAlertStore exceptionAlertStore
    ) {
        this.aopExceptionHandlingService = aopExceptionHandlingService;
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @GetMapping("/summary")
    public AopExceptionSummaryResponse summary() {
        return new AopExceptionSummaryResponse(
                "스프링 AOP는 예외가 발생한 지점을 공통 관심사로 분리해 로깅, 알림, 감사 기록을 한곳에서 처리할 수 있습니다.",
                List.of("Join Point", "Pointcut", "Advice", "AfterThrowing", "Proxy"),
                List.of(
                        "@AfterThrowing은 대상 메서드가 예외를 던진 직후 실행됩니다.",
                        "비즈니스 서비스는 예외를 던지는 책임에 집중하고, 공통 대응은 Aspect가 맡습니다.",
                        "예외 알림 로직을 서비스마다 복붙하지 않아도 되어 변경 비용이 줄어듭니다."
                )
        );
    }

    @GetMapping("/incidents/{incidentId}/recovery")
    public AopExceptionRecoveryResponse recoverIncident(
            @PathVariable String incidentId,
            @RequestParam(defaultValue = "ops-engineer") String operatorId,
            @RequestParam(defaultValue = "false") boolean triggerFailure
    ) {
        return AopExceptionRecoveryResponse.from(
                aopExceptionHandlingService.recoverIncident(incidentId, operatorId, triggerFailure),
                exceptionAlertStore.getEntries().size()
        );
    }

    @GetMapping("/alerts/latest")
    public AopExceptionAlertHistoryResponse latestAlerts() {
        return new AopExceptionAlertHistoryResponse(exceptionAlertStore.getEntries());
    }

    @GetMapping("/health")
    public String health() {
        return aopExceptionHandlingService.healthCheck();
    }
}
