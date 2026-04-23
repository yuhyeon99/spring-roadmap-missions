package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmupOperationTemplate extends AbstractOperationTemplate {

    @Override
    public String jobType() {
        return "cache-warmup";
    }

    @Override
    public String jobName() {
        return "캐시 예열 작업";
    }

    @Override
    protected void validateTarget(String target, List<String> steps) {
        if (!target.contains("cache")) {
            throw new IllegalArgumentException("cache-warmup 작업의 target은 cache 문자열을 포함해야 합니다.");
        }
        steps.add("3. 개별 검증 단계: 캐시 대상 영역 확인 완료");
    }

    @Override
    protected void prepare(String target, String operator, List<String> steps) {
        steps.add("4. 개별 준비 단계: %s 영역의 키 목록을 수집".formatted(target));
    }

    @Override
    protected String executeCore(String target, String operator, List<String> steps) {
        steps.add("6. 개별 실행 단계: 캐시 프리로드 요청 전송");
        return "%s 영역의 데이터를 미리 적재해 초기 응답 속도를 높였습니다.".formatted(target);
    }

    @Override
    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 개별 후처리 단계: 캐시 히트율 모니터링 등록");
    }
}
