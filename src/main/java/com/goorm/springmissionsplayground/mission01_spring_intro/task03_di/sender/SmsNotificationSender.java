package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public String send(String message) {
        return "[SMS] " + message;
    }
}
