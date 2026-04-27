package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto.AspectLogHistoryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto.ReportGenerationResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.AspectLoggingReportService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task05/aspect-logging")
public class AspectLoggingController {

    private final AspectLoggingReportService aspectLoggingReportService;
    private final AspectLogStore aspectLogStore;

    public AspectLoggingController(
            AspectLoggingReportService aspectLoggingReportService,
            AspectLogStore aspectLogStore
    ) {
        this.aspectLoggingReportService = aspectLoggingReportService;
        this.aspectLogStore = aspectLogStore;
    }

    @GetMapping("/reports/{reportId}")
    public ReportGenerationResponse generateReport(
            @PathVariable String reportId,
            @RequestParam(defaultValue = "ops-team") String operator,
            @RequestParam(defaultValue = "true") boolean includeDraftSection
    ) {
        return ReportGenerationResponse.from(
                aspectLoggingReportService.generateReport(reportId, operator, includeDraftSection)
        );
    }

    @GetMapping("/logs/latest")
    public AspectLogHistoryResponse latestLogs() {
        return new AspectLogHistoryResponse(aspectLogStore.getEntries());
    }

    @GetMapping("/health")
    public String health() {
        return aspectLoggingReportService.healthCheck();
    }
}
