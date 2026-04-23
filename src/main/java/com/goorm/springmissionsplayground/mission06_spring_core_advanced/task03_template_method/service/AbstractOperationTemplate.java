package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOperationTemplate {

    public final TemplateJobResult execute(String target, String operator) {
        List<String> steps = new ArrayList<>();

        steps.add("1. 공통 시작 단계: jobType=%s, operator=%s".formatted(jobType(), operator));
        validateCommonInput(target, operator);
        steps.add("2. 공통 입력 검증 완료");

        validateTarget(target, steps);
        prepare(target, operator, steps);

        steps.add("5. 공통 본 실행 단계 진입");
        String resultMessage = executeCore(target, operator, steps);

        afterExecute(target, operator, steps);
        steps.add("8. 공통 종료 단계: 결과 응답 조합 완료");

        return new TemplateJobResult(
                jobType(),
                jobName(),
                target,
                operator,
                "SUCCESS",
                resultMessage,
                steps
        );
    }

    public abstract String jobType();

    public abstract String jobName();

    protected abstract void validateTarget(String target, List<String> steps);

    protected abstract void prepare(String target, String operator, List<String> steps);

    protected abstract String executeCore(String target, String operator, List<String> steps);

    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 공통 후처리 단계: 실행 로그 정리");
    }

    private void validateCommonInput(String target, String operator) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target 파라미터는 비어 있을 수 없습니다.");
        }

        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator 파라미터는 비어 있을 수 없습니다.");
        }
    }
}
