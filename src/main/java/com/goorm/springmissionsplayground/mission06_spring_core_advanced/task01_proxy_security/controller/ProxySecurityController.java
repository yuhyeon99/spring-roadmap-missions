package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.AccessRole;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service.ProtectedProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task01/proxy-security")
public class ProxySecurityController {

    private final ProtectedProjectService protectedProjectService;

    public ProxySecurityController(ProtectedProjectService protectedProjectService) {
        this.protectedProjectService = protectedProjectService;
    }

    @GetMapping("/projects/{projectId}")
    public ProjectOperationPayload viewProject(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "guest-user") String userId,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam(defaultValue = "true") boolean authenticated
    ) {
        return protectedProjectService.viewProject(projectId, createContext(userId, role, authenticated));
    }

    @PostMapping("/projects/{projectId}/rotate-secrets")
    public ProjectOperationPayload rotateSecrets(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "ops-admin") String userId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(defaultValue = "true") boolean authenticated
    ) {
        return protectedProjectService.rotateSecrets(projectId, createContext(userId, role, authenticated));
    }

    private SecurityRequestContext createContext(String userId, String role, boolean authenticated) {
        return new SecurityRequestContext(userId, AccessRole.from(role), authenticated);
    }
}
