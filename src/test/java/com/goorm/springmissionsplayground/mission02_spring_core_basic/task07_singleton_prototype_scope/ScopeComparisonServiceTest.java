package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopeComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopePairResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.service.ScopeComparisonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScopeComparisonServiceTest {

    @Autowired
    private ScopeComparisonService scopeComparisonService;

    @Test
    void compare_verifiesSingletonAndPrototypeBehavior() {
        ScopeComparisonResponse response = scopeComparisonService.compare();

        ScopePairResult singleton = response.getSingletonScope();
        ScopePairResult injectedPrototype = response.getPrototypeInjectedIntoSingleton();
        ScopePairResult providerPrototype = response.getPrototypeFromProvider();

        assertThat(singleton.isSameInstance()).isTrue();
        assertThat(singleton.getSecondCallCount()).isEqualTo(singleton.getFirstCallCount() + 1);

        assertThat(injectedPrototype.isSameInstance()).isTrue();
        assertThat(injectedPrototype.getSecondCallCount()).isEqualTo(injectedPrototype.getFirstCallCount() + 1);

        assertThat(providerPrototype.isSameInstance()).isFalse();
        assertThat(providerPrototype.getFirstCallCount()).isEqualTo(1);
        assertThat(providerPrototype.getSecondCallCount()).isEqualTo(1);
    }
}
