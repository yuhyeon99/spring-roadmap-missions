package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.annotation.TrackExecution;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AspectDemoService {

    @TrackExecution
    public String buildSummary(String topic) {
        String normalizedTopic = normalize(topic);
        return normalizedTopic + " 학습 요청에 대해 애스펙트 로깅이 적용되었습니다.";
    }

    public String ping() {
        return "ok";
    }

    private String normalize(String topic) {
        if (!StringUtils.hasText(topic)) {
            throw new IllegalArgumentException("topic은 필수입니다.");
        }
        return topic.trim().toLowerCase(Locale.ROOT);
    }
}
