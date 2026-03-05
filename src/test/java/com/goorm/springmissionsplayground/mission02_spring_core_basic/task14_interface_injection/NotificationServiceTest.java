package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

    @Test
    @DisplayName("채널 문자열로 구현체를 선택해 알림을 보낸다")
    void notifyByChannel() {
        NotifyRequest request = new NotifyRequest();
        request.setTo("user@example.com");
        request.setMessage("환영합니다");
        request.setChannel("email");

        NotifyResponse response = notificationService.notify(request);

        assertThat(response.getChannel()).isEqualTo("email");
        assertThat(response.getTo()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("지원하지 않는 채널이면 예외가 발생한다")
    void unsupportedChannel() {
        NotifyRequest request = new NotifyRequest();
        request.setTo("user");
        request.setMessage("hello");
        request.setChannel("slack");

        assertThatThrownBy(() -> notificationService.notify(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("지원하지 않는 채널");
    }
}
