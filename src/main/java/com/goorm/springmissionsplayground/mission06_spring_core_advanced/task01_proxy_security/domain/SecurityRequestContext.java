package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain;

public class SecurityRequestContext {

    private final String userId;
    private final AccessRole role;
    private final boolean authenticated;

    public SecurityRequestContext(String userId, AccessRole role, boolean authenticated) {
        this.userId = userId;
        this.role = role;
        this.authenticated = authenticated;
    }

    public String getUserId() {
        return userId;
    }

    public AccessRole getRole() {
        return role;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
