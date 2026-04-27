package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.ReportGenerationResult;
import java.util.List;

public class ReportGenerationResponse {

    private final String reportId;
    private final String operator;
    private final boolean includeDraftSection;
    private final String status;
    private final String resultMessage;
    private final String digest;
    private final List<String> generatedSections;

    public ReportGenerationResponse(
            String reportId,
            String operator,
            boolean includeDraftSection,
            String status,
            String resultMessage,
            String digest,
            List<String> generatedSections
    ) {
        this.reportId = reportId;
        this.operator = operator;
        this.includeDraftSection = includeDraftSection;
        this.status = status;
        this.resultMessage = resultMessage;
        this.digest = digest;
        this.generatedSections = List.copyOf(generatedSections);
    }

    public static ReportGenerationResponse from(ReportGenerationResult result) {
        return new ReportGenerationResponse(
                result.getReportId(),
                result.getOperator(),
                result.isIncludeDraftSection(),
                result.getStatus(),
                result.getResultMessage(),
                result.getDigest(),
                result.getGeneratedSections()
        );
    }

    public String getReportId() {
        return reportId;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isIncludeDraftSection() {
        return includeDraftSection;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getDigest() {
        return digest;
    }

    public List<String> getGeneratedSections() {
        return generatedSections;
    }
}
