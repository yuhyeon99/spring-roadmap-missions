package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.TemplateJobResult;
import java.util.List;

public class TemplateJobResponse {

    private final String jobType;
    private final String jobName;
    private final String target;
    private final String operator;
    private final String status;
    private final String resultMessage;
    private final List<String> steps;

    public TemplateJobResponse(
            String jobType,
            String jobName,
            String target,
            String operator,
            String status,
            String resultMessage,
            List<String> steps
    ) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.target = target;
        this.operator = operator;
        this.status = status;
        this.resultMessage = resultMessage;
        this.steps = List.copyOf(steps);
    }

    public static TemplateJobResponse from(TemplateJobResult result) {
        return new TemplateJobResponse(
                result.getJobType(),
                result.getJobName(),
                result.getTarget(),
                result.getOperator(),
                result.getStatus(),
                result.getResultMessage(),
                result.getSteps()
        );
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTarget() {
        return target;
    }

    public String getOperator() {
        return operator;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<String> getSteps() {
        return steps;
    }
}
