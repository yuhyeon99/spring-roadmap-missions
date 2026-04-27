package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.aspect.ExceptionAlertAspect;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.AopExceptionHandlingService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
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
class AopExceptionHandlingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AopExceptionHandlingService aopExceptionHandlingService;

    @Autowired
    private ExceptionAlertStore exceptionAlertStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        exceptionAlertStore.reset();
    }

    @Test
    void exceptionHandledService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(aopExceptionHandlingService)).isTrue();
    }

    @Test
    void recoverIncident_whenFailure_thenAspectLogsAndStoresAlert() {
        Logger logger = (Logger) LoggerFactory.getLogger(ExceptionAlertAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            assertThatThrownBy(() -> aopExceptionHandlingService.recoverIncident("incident-500", "ops-bot", true))
                    .isInstanceOf(IncidentRecoveryException.class)
                    .hasMessage("incident-500 장애 복구 승인 중 외부 알림 연동이 실패했습니다.");

            assertThat(listAppender.list)
                    .hasSize(1)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("[TASK08-AOP][ALERT][incident-recovery]")
                            && message.contains("AopExceptionHandlingService.recoverIncident")
                            && message.contains("target=slack://ops-critical-alert")
                            && message.contains("IncidentRecoveryException"));

            assertThat(exceptionAlertStore.getEntries())
                    .hasSize(1)
                    .extracting("phase", "operation", "method", "alertTarget", "exceptionType")
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(
                                    "AFTER_THROWING",
                                    "incident-recovery",
                                    "AopExceptionHandlingService.recoverIncident",
                                    "slack://ops-critical-alert",
                                    "IncidentRecoveryException"
                            )
                    );
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    void summary_returnsAopExceptionKeywords() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords", hasSize(5)))
                .andExpect(jsonPath("$.keywords[3]").value("AfterThrowing"))
                .andExpect(jsonPath("$.notes[1]").value("비즈니스 서비스는 예외를 던지는 책임에 집중하고, 공통 대응은 Aspect가 맡습니다."));
    }

    @Test
    void recoverIncident_whenSuccess_thenReturnsResultWithoutAlerts() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/incidents/incident-101/recovery")
                        .param("operatorId", "ops-engineer")
                        .param("triggerFailure", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("incident-101"))
                .andExpect(jsonPath("$.operatorId").value("ops-engineer"))
                .andExpect(jsonPath("$.status").value("RECOVERY_COMPLETED"))
                .andExpect(jsonPath("$.alertCount").value(0))
                .andExpect(jsonPath("$.executedSteps[2]").value("운영자 승인 후 복구 작업을 완료했습니다."));

        mockMvc.perform(get("/mission06/task08/aop-exception-handling/alerts/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void recoverIncident_whenFailure_thenReturnsErrorResponseAndLatestAlert() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/incidents/incident-900/recovery")
                        .param("operatorId", "ops-engineer")
                        .param("triggerFailure", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("incident-900 장애 복구 승인 중 외부 알림 연동이 실패했습니다."))
                .andExpect(jsonPath("$.alertCount").value(1))
                .andExpect(jsonPath("$.latestAlert.phase").value("AFTER_THROWING"))
                .andExpect(jsonPath("$.latestAlert.operation").value("incident-recovery"))
                .andExpect(jsonPath("$.latestAlert.alertTarget").value("slack://ops-critical-alert"))
                .andExpect(jsonPath("$.latestAlert.exceptionType").value("IncidentRecoveryException"));

        mockMvc.perform(get("/mission06/task08/aop-exception-handling/alerts/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].method").value("AopExceptionHandlingService.recoverIncident"))
                .andExpect(jsonPath("$.entries[0].message").value("incident-900 장애 복구 승인 중 외부 알림 연동이 실패했습니다."));
    }

    @Test
    void nonAnnotatedMethod_doesNotTriggerAlert() {
        aopExceptionHandlingService.healthCheck();

        assertThat(exceptionAlertStore.getEntries()).isEmpty();
    }
}
