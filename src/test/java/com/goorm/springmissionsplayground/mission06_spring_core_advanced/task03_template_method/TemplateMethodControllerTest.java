package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TemplateMethodControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void templateMethod_execute_isFinal() throws Exception {
        Method execute = Class.forName(
                        "com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.AbstractOperationTemplate"
                )
                .getDeclaredMethod("execute", String.class, String.class);

        org.assertj.core.api.Assertions.assertThat(Modifier.isFinal(execute.getModifiers())).isTrue();
    }

    @Test
    void jobs_returnsSupportedTemplates() throws Exception {
        mockMvc.perform(get("/mission06/task03/template-method/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedJobTypes", hasSize(2)))
                .andExpect(jsonPath("$.supportedJobTypes[0]").value("cache-warmup"))
                .andExpect(jsonPath("$.supportedJobTypes[1]").value("report-publish"));
    }

    @Test
    void runCacheWarmup_usesTemplateFlow() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/cache-warmup/run")
                        .param("target", "edge-cache")
                        .param("operator", "ops-kim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("cache-warmup"))
                .andExpect(jsonPath("$.jobName").value("캐시 예열 작업"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.steps", hasSize(8)))
                .andExpect(jsonPath("$.steps[0]").value("1. 공통 시작 단계: jobType=cache-warmup, operator=ops-kim"))
                .andExpect(jsonPath("$.steps[2]").value("3. 개별 검증 단계: 캐시 대상 영역 확인 완료"))
                .andExpect(jsonPath("$.steps[7]").value("8. 공통 종료 단계: 결과 응답 조합 완료"));
    }

    @Test
    void runReportPublish_usesDifferentOverriddenSteps() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/report-publish/run")
                        .param("target", "monthly-sales")
                        .param("operator", "data-lee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("report-publish"))
                .andExpect(jsonPath("$.jobName").value("보고서 발행 작업"))
                .andExpect(jsonPath("$.resultMessage").value("monthly-sales 보고서를 생성하고 구독자 채널로 발행했습니다."))
                .andExpect(jsonPath("$.steps[2]").value("3. 개별 검증 단계: 보고서 식별자와 발행 범위 확인 완료"))
                .andExpect(jsonPath("$.steps[6]").value("7. 개별 후처리 단계: 발행 이력과 재시도 메타데이터 저장"));
    }

    @Test
    void runUnknownJobType_returns400() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/unknown/run")
                        .param("target", "edge-cache")
                        .param("operator", "ops-kim"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 jobType 입니다. 사용 가능 값: cache-warmup, report-publish"));
    }
}
