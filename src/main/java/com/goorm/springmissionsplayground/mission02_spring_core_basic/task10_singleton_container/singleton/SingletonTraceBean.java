package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SingletonTraceBean {

    private final String instanceId = UUID.randomUUID().toString();
    private int callCount = 0;

    public synchronized int touch() {
        callCount++;
        return callCount;
    }

    public synchronized int getCallCount() {
        return callCount;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
