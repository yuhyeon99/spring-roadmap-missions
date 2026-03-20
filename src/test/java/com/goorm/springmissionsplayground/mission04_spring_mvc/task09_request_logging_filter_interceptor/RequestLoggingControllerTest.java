package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.filter.RequestLoggingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class RequestLoggingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RequestLoggingFilter requestLoggingFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(requestLoggingFilter)
                .build();
    }

    @Test
    @DisplayName("task09 요청은 필터와 인터셉터 로그를 함께 남긴다")
    void logsRequestWithFilterAndInterceptor(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/mission04/task09/logs/requests").param("topic", "request-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.requestUri").value("/mission04/task09/logs/requests"))
                .andExpect(jsonPath("$.topic").value("request-log"))
                .andExpect(jsonPath("$.message").value("필터와 인터셉터 로그를 함께 확인할 수 있습니다."));

        assertThat(output.getOut()).contains("[Task09Filter][REQUEST] method=GET, uri=/mission04/task09/logs/requests");
        assertThat(output.getOut()).contains("[Task09Interceptor][PRE_HANDLE] method=GET, uri=/mission04/task09/logs/requests, handler=RequestLoggingController.inspectRequest");
        assertThat(output.getOut()).contains("[Task09Interceptor][POST_HANDLE] method=GET, uri=/mission04/task09/logs/requests, view=response-body, status=200");
        assertThat(output.getOut()).contains("[Task09Interceptor][AFTER_COMPLETION] method=GET, uri=/mission04/task09/logs/requests, handler=RequestLoggingController.inspectRequest, status=200");
        assertThat(output.getOut()).contains("[Task09Filter][RESPONSE] method=GET, uri=/mission04/task09/logs/requests, status=200");
    }

    @Test
    @DisplayName("설정한 경로 밖의 요청은 task09 로깅 대상에서 제외된다")
    void doesNotLogOutsideConfiguredPath(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).doesNotContain("[Task09Filter]");
        assertThat(output.getOut()).doesNotContain("[Task09Interceptor]");
    }
}
