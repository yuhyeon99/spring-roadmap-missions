package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import org.springframework.stereotype.Component;

@Component
public class ProjectionWorkloadExecutor {

    public String buildProjection(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload는 비어 있을 수 없습니다.");
        }

        long checksum = 23L;
        int vowelCount = 0;

        for (int round = 0; round < 6; round++) {
            for (int index = 0; index < payload.length(); index++) {
                char current = payload.charAt(index);
                checksum = (checksum * 33L) + current + round;
                if (isVowel(current)) {
                    vowelCount++;
                }
            }
            checksum ^= (checksum >>> 9);
        }

        String preview = payload.substring(0, Math.min(10, payload.length()));
        String suffix = payload.substring(Math.max(0, payload.length() - Math.min(10, payload.length())));
        return "projection[length=%d, checksum=%s, vowels=%d, preview=%s...%s]"
                .formatted(payload.length(), Long.toUnsignedString(checksum, 16), vowelCount, preview, suffix);
    }

    private boolean isVowel(char value) {
        return switch (Character.toLowerCase(value)) {
            case 'a', 'e', 'i', 'o', 'u' -> true;
            default -> false;
        };
    }
}
