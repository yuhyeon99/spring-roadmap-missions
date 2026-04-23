package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReportPublishOperationTemplate extends AbstractOperationTemplate {

    @Override
    public String jobType() {
        return "report-publish";
    }

    @Override
    public String jobName() {
        return "보고서 발행 작업";
    }

    @Override
    protected void validateTarget(String target, List<String> steps) {
        if (target.length() < 5) {
            throw new IllegalArgumentException("report-publish 작업의 target은 5자 이상이어야 합니다.");
        }
        steps.add("3. 개별 검증 단계: 보고서 식별자와 발행 범위 확인 완료");
    }

    @Override
    protected void prepare(String target, String operator, List<String> steps) {
        steps.add("4. 개별 준비 단계: 보고서 메타데이터와 구독자 목록 로딩");
    }

    @Override
    protected String executeCore(String target, String operator, List<String> steps) {
        steps.add("6. 개별 실행 단계: PDF 생성 후 구독자에게 발행");
        return "%s 보고서를 생성하고 구독자 채널로 발행했습니다.".formatted(target);
    }

    @Override
    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 개별 후처리 단계: 발행 이력과 재시도 메타데이터 저장");
    }
}
