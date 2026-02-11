package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto;

public class NotificationRequest {
    private String message;

    public NotificationRequest() {
    }

    public NotificationRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
