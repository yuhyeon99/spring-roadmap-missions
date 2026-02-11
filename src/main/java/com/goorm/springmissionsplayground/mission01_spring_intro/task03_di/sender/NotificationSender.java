package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

public interface NotificationSender {
    String channel();
    String send(String message);
}
