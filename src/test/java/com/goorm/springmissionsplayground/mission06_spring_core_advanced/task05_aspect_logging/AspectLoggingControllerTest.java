package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.aspect.AspectMethodLoggingAspect;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.AspectLoggingReportService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AspectLoggingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AspectLoggingReportService aspectLoggingReportService;

    @Autowired
    private AspectLogStore aspectLogStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        aspectLogStore.reset();
    }

    @Test
    void trackedService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(aspectLoggingReportService)).isTrue();
    }

    @Test
    void generateReport_logsBeforeAndAfterWithResultAndElapsedTime() {
        Logger logger = (Logger) LoggerFactory.getLogger(AspectMethodLoggingAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            aspectLoggingReportService.generateReport("report-2026-05", "audit-bot", true);

            assertThat(listAppender.list)
                    .hasSize(2)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("[TASK05-AOP][START][report-generation]")
                            && message.contains("AspectLoggingReportService.generateReport")
                            && message.contains("args=[report-2026-05, audit-bot, true]"))
                    .anyMatch(message -> message.contains("[TASK05-AOP][END][report-generation]")
                            && message.contains("ReportGenerationResult{reportId='report-2026-05'")
                            && message.contains("elapsedMs="));

            assertThat(aspectLogStore.getEntries())
                    .hasSize(2)
                    .extracting("phase")
                    .containsExactly("START", "END");

            assertThat(aspectLogStore.getEntries().get(1).getDetail())
                    .contains("report-2026-05");
            assertThat(aspectLogStore.getEntries().get(1).getElapsedMs())
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0L);
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    void nonAnnotatedMethod_doesNotCreateAspectLogs() {
        aspectLoggingReportService.healthCheck();

        assertThat(aspectLogStore.getEntries()).isEmpty();
    }

    @Test
    void reportEndpoint_andLatestLogEndpoint_returnExecutionResultAndStoredLogs() throws Exception {
        mockMvc.perform(get("/mission06/task05/aspect-logging/reports/report-2026-ops")
                        .param("operator", "ops-team")
                        .param("includeDraftSection", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("report-2026-ops"))
                .andExpect(jsonPath("$.operator").value("ops-team"))
                .andExpect(jsonPath("$.includeDraftSection").value(false))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.generatedSections[0]").value("overview"))
                .andExpect(jsonPath("$.generatedSections[2]").value("timing-analysis"));

        mockMvc.perform(get("/mission06/task05/aspect-logging/logs/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.entries[0].phase").value("START"))
                .andExpect(jsonPath("$.entries[0].operation").value("report-generation"))
                .andExpect(jsonPath("$.entries[1].phase").value("END"))
                .andExpect(jsonPath("$.entries[1].method").value("AspectLoggingReportService.generateReport"))
                .andExpect(jsonPath("$.entries[1].detail").value(org.hamcrest.Matchers.containsString("report-2026-ops")))
                .andExpect(jsonPath("$.entries[1].elapsedMs").isNumber());
    }
}
