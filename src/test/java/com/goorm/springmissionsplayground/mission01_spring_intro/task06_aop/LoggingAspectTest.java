package com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service.MemberTxService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoggingAspectTest {

    @Autowired
    MemberTxService memberTxService;

    @Test
    void aop_logs_execution_time() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop.aspect.LoggingAspect");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            memberTxService.findAll(); // 대상 메서드 실행

            assertThat(listAppender.list)
                    .anyMatch(event -> event.getFormattedMessage().contains("MemberTxService.findAll"));
        } finally {
            logger.detachAppender(listAppender);
        }
    }
}
