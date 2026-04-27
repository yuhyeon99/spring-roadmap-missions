package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import java.util.List;

public class ThreadLocalConnectionConceptResponse {

    private final String topic;
    private final List<String> principles;

    public ThreadLocalConnectionConceptResponse(String topic, List<String> principles) {
        this.topic = topic;
        this.principles = List.copyOf(principles);
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getPrinciples() {
        return principles;
    }
}
