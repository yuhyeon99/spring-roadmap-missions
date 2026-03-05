package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("task15ExecutionLogStore")
public class ExecutionLogStore {

    private final List<String> logs = new ArrayList<>();

    public void add(String log) {
        logs.add(log);
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void clear() {
        logs.clear();
    }
}
