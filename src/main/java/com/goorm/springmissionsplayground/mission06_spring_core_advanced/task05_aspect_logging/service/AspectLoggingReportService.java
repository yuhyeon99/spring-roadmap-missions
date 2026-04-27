package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.annotation.LoggableOperation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AspectLoggingReportService {

    @LoggableOperation("report-generation")
    public ReportGenerationResult generateReport(String reportId, String operator, boolean includeDraftSection) {
        validate(reportId, operator);

        List<String> generatedSections = new ArrayList<>();
        generatedSections.add("overview");
        generatedSections.add("aop-log-summary");
        generatedSections.add("timing-analysis");

        if (includeDraftSection) {
            generatedSections.add("draft-appendix");
        }

        String digest = reportId + "|" + operator + "|" + generatedSections.size();
        String resultMessage = operator + " 사용자가 " + reportId + " 보고서를 생성했습니다.";

        return new ReportGenerationResult(
                reportId,
                operator,
                includeDraftSection,
                "COMPLETED",
                resultMessage,
                digest,
                generatedSections
        );
    }

    public String healthCheck() {
        return "aspect-logging-ready";
    }

    private void validate(String reportId, String operator) {
        if (reportId == null || reportId.isBlank()) {
            throw new IllegalArgumentException("reportId는 비어 있을 수 없습니다.");
        }
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator는 비어 있을 수 없습니다.");
        }
    }
}
