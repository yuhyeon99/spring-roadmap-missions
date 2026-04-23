package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProxySecurityControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void viewProject_whenAuthenticatedUser_thenReturnsSecurityTrace() throws Exception {
        mockMvc.perform(get("/mission06/task01/proxy-security/projects/project-alpha")
                        .param("userId", "analyst-kim")
                        .param("role", "USER")
                        .param("authenticated", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value("project-alpha"))
                .andExpect(jsonPath("$.action").value("프로젝트 조회"))
                .andExpect(jsonPath("$.requestedBy").value("analyst-kim"))
                .andExpect(jsonPath("$.requestedRole").value("USER"))
                .andExpect(jsonPath("$.requiredRole").value("USER"))
                .andExpect(jsonPath("$.proxyApplied").value(true))
                .andExpect(jsonPath("$.securityChecks", hasSize(8)))
                .andExpect(jsonPath("$.securityChecks[0]").value("프록시 진입: 프로젝트 조회"))
                .andExpect(jsonPath("$.securityChecks[7]").value("사후 보안 로그 기록 완료"));
    }

    @Test
    void rotateSecrets_whenAdmin_thenReturnsProtectedResult() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "ops-admin")
                        .param("role", "ADMIN")
                        .param("authenticated", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("시크릿 재발급"))
                .andExpect(jsonPath("$.requestedRole").value("ADMIN"))
                .andExpect(jsonPath("$.requiredRole").value("ADMIN"))
                .andExpect(jsonPath("$.resultMessage").value("프로젝트 project-alpha의 배포 시크릿을 재발급하고 감사 로그를 남겼습니다."));
    }

    @Test
    void rotateSecrets_whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "anonymous")
                        .param("role", "ADMIN")
                        .param("authenticated", "false"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("시크릿 재발급 작업은 로그인 후에만 실행할 수 있습니다."));
    }

    @Test
    void rotateSecrets_whenRoleIsUser_thenReturns403() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "team-user")
                        .param("role", "USER")
                        .param("authenticated", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("시크릿 재발급 작업에는 ADMIN 권한이 필요합니다."));
    }
}
