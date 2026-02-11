package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String format(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return String.format("[%s] %s", timestamp, message);
    }
}
