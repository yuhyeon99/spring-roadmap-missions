package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service.AutoInjectionComparisonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutoInjectionComparisonServiceTest {

    @Autowired
    private AutoInjectionComparisonService autoInjectionComparisonService;

    @Test
    void compare_returnsDifferentResultsByInjectionStrategy() {
        AutoInjectionComparisonResponse response = autoInjectionComparisonService.compare(20000);

        assertThat(response.getAmount()).isEqualTo(20000);
        assertThat(response.getComparisons()).hasSize(3);

        InjectionCaseResult autowired = response.getComparisons().get(0);
        InjectionCaseResult qualifier = response.getComparisons().get(1);
        InjectionCaseResult primary = response.getComparisons().get(2);

        assertThat(autowired.getInjectionType()).isEqualTo("@Autowired");
        assertThat(autowired.getInjectedBean()).isEqualTo("amountFormatter");
        assertThat(autowired.getResult()).isEqualTo("포맷 결과: 20,000원");

        assertThat(qualifier.getInjectionType()).isEqualTo("@Autowired + @Qualifier");
        assertThat(qualifier.getInjectedBean()).isEqualTo("fixedDiscountPolicy");
        assertThat(qualifier.getResult()).isEqualTo("할인 금액: 1,000원");

        assertThat(primary.getInjectionType()).isEqualTo("@Autowired + @Primary");
        assertThat(primary.getInjectedBean()).isEqualTo("rateDiscountPolicy");
        assertThat(primary.getResult()).isEqualTo("할인 금액: 2,000원");
    }
}
