package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain;

public enum ProjectSecurityAction {

    VIEW_PROJECT("프로젝트 조회", AccessRole.USER),
    ROTATE_SECRETS("시크릿 재발급", AccessRole.ADMIN);

    private final String actionLabel;
    private final AccessRole requiredRole;

    ProjectSecurityAction(String actionLabel, AccessRole requiredRole) {
        this.actionLabel = actionLabel;
        this.requiredRole = requiredRole;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public AccessRole getRequiredRole() {
        return requiredRole;
    }
}
