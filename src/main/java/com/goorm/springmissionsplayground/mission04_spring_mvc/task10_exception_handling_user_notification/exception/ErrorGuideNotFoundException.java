package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception;

public class ErrorGuideNotFoundException extends RuntimeException {

    private final Long guideId;

    public ErrorGuideNotFoundException(Long guideId) {
        super("요청한 안내 페이지를 찾을 수 없습니다. 다시 목록에서 유효한 항목을 선택해 주세요.");
        this.guideId = guideId;
    }

    public Long getGuideId() {
        return guideId;
    }
}
