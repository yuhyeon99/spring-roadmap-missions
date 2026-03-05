package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailNotificationChannel implements NotificationChannel {

    private final List<String> deliveredMessages = new ArrayList<>();

    @Override
    public void notify(String userId, String message) {
        deliveredMessages.add("EMAIL|" + userId + "|" + message);
    }

    public List<String> deliveredMessages() {
        return Collections.unmodifiableList(deliveredMessages);
    }
}
