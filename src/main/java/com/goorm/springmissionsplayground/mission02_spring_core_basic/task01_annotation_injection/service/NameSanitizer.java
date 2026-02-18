package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import org.springframework.stereotype.Component;

@Component
public class NameSanitizer {

    public String sanitize(String rawName) {
        if (rawName == null) {
            return "손님";
        }

        String normalized = rawName.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? "손님" : normalized;
    }
}
