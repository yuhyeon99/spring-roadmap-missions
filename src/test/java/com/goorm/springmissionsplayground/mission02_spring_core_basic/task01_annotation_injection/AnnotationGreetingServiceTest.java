package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service.AnnotationGreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AnnotationGreetingServiceTest {

    @Autowired
    private AnnotationGreetingService greetingService;

    @Test
    void greet_usesAutowiredAndInjectInjectedBeans() {
        GreetingResponse response = greetingService.greet("  스프링   학습자  ");

        assertThat(response.getMessage()).isEqualTo("안녕하세요, 스프링 학습자님. 애너테이션 기반 빈 주입이 정상 동작했습니다.");
        assertThat(response.getSelectedPolicy()).isEqualTo("formalGreetingPolicy");
        assertThat(response.getInjectionType()).isEqualTo("@Autowired + @Inject");
    }

    @Test
    void greet_usesFallbackNameWhenInputIsBlank() {
        GreetingResponse response = greetingService.greet("   ");

        assertThat(response.getMessage()).contains("손님");
    }
}
