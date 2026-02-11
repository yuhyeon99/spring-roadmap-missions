package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public String send(String message) {
        return "[EMAIL] " + message;
    }
}
