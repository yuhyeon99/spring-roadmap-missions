package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.sender;

import org.springframework.stereotype.Component;

@Component("smsSender")
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(String to, String message) {
        System.out.printf("[SMS] to=%s msg=%s%n", to, message);
    }

    @Override
    public String channel() {
        return "sms";
    }
}
