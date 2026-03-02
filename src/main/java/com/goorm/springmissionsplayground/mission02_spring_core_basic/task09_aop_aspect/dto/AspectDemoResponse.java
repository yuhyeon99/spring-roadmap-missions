package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.dto;

public class AspectDemoResponse {

    private final String topic;
    private final String result;

    public AspectDemoResponse(String topic, String result) {
        this.topic = topic;
        this.result = result;
    }

    public String getTopic() {
        return topic;
    }

    public String getResult() {
        return result;
    }
}
