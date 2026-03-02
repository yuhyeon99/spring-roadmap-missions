package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto;

public class SingletonCheckResponse {

    private final String firstLookupInstanceId;
    private final String secondLookupInstanceId;
    private final int firstLookupIdentityHash;
    private final int secondLookupIdentityHash;
    private final boolean sameInstance;
    private final int firstCallCount;
    private final int secondCallCount;
    private final String summary;

    public SingletonCheckResponse(
            String firstLookupInstanceId,
            String secondLookupInstanceId,
            int firstLookupIdentityHash,
            int secondLookupIdentityHash,
            boolean sameInstance,
            int firstCallCount,
            int secondCallCount,
            String summary
    ) {
        this.firstLookupInstanceId = firstLookupInstanceId;
        this.secondLookupInstanceId = secondLookupInstanceId;
        this.firstLookupIdentityHash = firstLookupIdentityHash;
        this.secondLookupIdentityHash = secondLookupIdentityHash;
        this.sameInstance = sameInstance;
        this.firstCallCount = firstCallCount;
        this.secondCallCount = secondCallCount;
        this.summary = summary;
    }

    public String getFirstLookupInstanceId() {
        return firstLookupInstanceId;
    }

    public String getSecondLookupInstanceId() {
        return secondLookupInstanceId;
    }

    public int getFirstLookupIdentityHash() {
        return firstLookupIdentityHash;
    }

    public int getSecondLookupIdentityHash() {
        return secondLookupIdentityHash;
    }

    public boolean isSameInstance() {
        return sameInstance;
    }

    public int getFirstCallCount() {
        return firstCallCount;
    }

    public int getSecondCallCount() {
        return secondCallCount;
    }

    public String getSummary() {
        return summary;
    }
}
