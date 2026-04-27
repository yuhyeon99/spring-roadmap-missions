package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service.SpringAopConceptSecurityService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support.AopAccessAuditStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class SpringAopConceptControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringAopConceptSecurityService springAopConceptSecurityService;

    @Autowired
    private AopAccessAuditStore aopAccessAuditStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        aopAccessAuditStore.reset();
    }

    @Test
    void securedService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(springAopConceptSecurityService)).isTrue();
    }

    @Test
    void summary_returnsAopConceptKeywords() throws Exception {
        mockMvc.perform(get("/mission06/task06/aop-concepts/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords", hasSize(5)))
                .andExpect(jsonPath("$.keywords[0]").value("Aspect"))
                .andExpect(jsonPath("$.notes[2]").value("Spring AOP는 프록시 기반이라 self-invocation은 기본적으로 가로채지 못합니다."));
    }

    @Test
    void viewDeploymentPlan_whenUserRole_thenReturnsAuditTrail() throws Exception {
        mockMvc.perform(get("/mission06/task06/aop-concepts/deployment-plans/plan-blue")
                        .param("operatorId", "release-user")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value("plan-blue"))
                .andExpect(jsonPath("$.action").value("배포 계획 조회"))
                .andExpect(jsonPath("$.currentRole").value("USER"))
                .andExpect(jsonPath("$.requiredRole").value("USER"))
                .andExpect(jsonPath("$.aopApplied").value(true))
                .andExpect(jsonPath("$.auditTrail", hasSize(4)))
                .andExpect(jsonPath("$.auditTrail[0].phase").value("BEFORE"))
                .andExpect(jsonPath("$.auditTrail[2].phase").value("GRANTED"))
                .andExpect(jsonPath("$.auditTrail[3].phase").value("AFTER_RETURNING"));
    }

    @Test
    void approveProductionDeployment_whenUserRole_thenReturns403() throws Exception {
        mockMvc.perform(get("/mission06/task06/aop-concepts/deployment-plans/plan-red/approval")
                        .param("operatorId", "release-user")
                        .param("role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("운영 배포 승인 작업에는 ADMIN 권한이 필요합니다."));
    }

    @Test
    void nonAnnotatedMethod_doesNotCreateAuditTrail() {
        springAopConceptSecurityService.conceptKeywords();

        assertThat(aopAccessAuditStore.getEntries()).isEmpty();
    }
}
