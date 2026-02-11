package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void notifyAllChannels_formatsMessageAndUsesAllSenders() {
        List<String> results = notificationService.notifyAllChannels("Test message");

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).anyMatch(msg -> msg.contains("EMAIL") || msg.contains("[EMAIL]"));
        assertThat(results).anyMatch(msg -> msg.contains("SMS") || msg.contains("[SMS]"));
    }
}
