package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.exception;

public class WorkshopCapacityExceededException extends Exception {

    private final String workshopCode;
    private final long requestedCount;
    private final long maxCapacity;

    public WorkshopCapacityExceededException(String workshopCode, long requestedCount, long maxCapacity) {
        super("워크숍 정원을 초과했습니다. workshopCode=%s, 현재 신청 수=%d, 최대 정원=%d"
                .formatted(workshopCode, requestedCount, maxCapacity));
        this.workshopCode = workshopCode;
        this.requestedCount = requestedCount;
        this.maxCapacity = maxCapacity;
    }

    public String getWorkshopCode() {
        return workshopCode;
    }

    public long getRequestedCount() {
        return requestedCount;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }
}
