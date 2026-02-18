package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy;

import org.springframework.stereotype.Component;

@Component("formalGreetingPolicy")
public class FormalGreetingPolicy implements GreetingPolicy {

    @Override
    public String createMessage(String name) {
        return "안녕하세요, " + name + "님. 애너테이션 기반 빈 주입이 정상 동작했습니다.";
    }
}
