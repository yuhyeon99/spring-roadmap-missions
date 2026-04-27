package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain;

import java.util.Locale;

public enum AopUserRole {

    GUEST(0),
    USER(1),
    MANAGER(2),
    ADMIN(3);

    private final int level;

    AopUserRole(int level) {
        this.level = level;
    }

    public boolean hasAtLeast(AopUserRole requiredRole) {
        return this.level >= requiredRole.level;
    }

    public static AopUserRole from(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("role 파라미터는 비어 있을 수 없습니다.");
        }

        try {
            return AopUserRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("지원하지 않는 role 입니다. 사용 가능 값: GUEST, USER, MANAGER, ADMIN");
        }
    }
}
