package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionConceptResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionDemoResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionPerformanceResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionStudyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task07/thread-local-connections")
public class ThreadLocalConnectionController {

    private final ThreadLocalConnectionStudyService threadLocalConnectionStudyService;

    public ThreadLocalConnectionController(ThreadLocalConnectionStudyService threadLocalConnectionStudyService) {
        this.threadLocalConnectionStudyService = threadLocalConnectionStudyService;
    }

    @GetMapping("/concepts")
    public ThreadLocalConnectionConceptResponse concepts() {
        return new ThreadLocalConnectionConceptResponse(
                "ThreadLocal은 현재 스레드 전용 저장소를 제공해 같은 요청 흐름 안에서 하나의 DB 연결을 재사용할 수 있게 합니다.",
                List.of(
                        "같은 스레드에서는 같은 Connection을 재사용합니다.",
                        "다른 스레드에서는 서로 다른 Connection이 바인딩됩니다.",
                        "작업이 끝나면 반드시 remove/close로 정리해야 메모리 누수와 잘못된 재사용을 막을 수 있습니다."
                )
        );
    }

    @GetMapping("/plans/{planId}/demo")
    public ThreadLocalConnectionDemoResponse demo(
            @PathVariable String planId,
            @RequestParam(defaultValue = "release-engineer") String operatorId
    ) {
        return ThreadLocalConnectionDemoResponse.from(
                threadLocalConnectionStudyService.demonstrateThreadBoundConnection(planId, operatorId)
        );
    }

    @GetMapping("/performance")
    public ThreadLocalConnectionPerformanceResponse performance(
            @RequestParam(defaultValue = "4") int workerCount,
            @RequestParam(defaultValue = "150") int iterationsPerWorker
    ) {
        return ThreadLocalConnectionPerformanceResponse.from(
                threadLocalConnectionStudyService.measurePerformance(workerCount, iterationsPerWorker)
        );
    }
}
