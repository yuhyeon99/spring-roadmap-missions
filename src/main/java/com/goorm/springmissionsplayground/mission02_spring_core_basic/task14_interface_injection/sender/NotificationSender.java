package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.sender;

public interface NotificationSender {

    void send(String to, String message);

    String channel();
}
