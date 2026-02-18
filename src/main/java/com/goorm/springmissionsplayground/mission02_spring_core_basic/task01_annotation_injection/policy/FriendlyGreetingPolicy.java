package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy;

import org.springframework.stereotype.Component;

@Component("friendlyGreetingPolicy")
public class FriendlyGreetingPolicy implements GreetingPolicy {

    @Override
    public String createMessage(String name) {
        return "반가워요, " + name + "님! 오늘도 즐겁게 스프링을 학습해봐요.";
    }
}
