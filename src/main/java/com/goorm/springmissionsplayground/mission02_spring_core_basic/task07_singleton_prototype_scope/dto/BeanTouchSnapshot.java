package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class BeanTouchSnapshot {

    private final String instanceId;
    private final int callCount;

    public BeanTouchSnapshot(String instanceId, int callCount) {
        this.instanceId = instanceId;
        this.callCount = callCount;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getCallCount() {
        return callCount;
    }
}
