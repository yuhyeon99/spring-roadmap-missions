package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service.SingletonContainerService;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton.SingletonTraceBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SingletonContainerServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SingletonContainerService singletonContainerService;

    @Test
    void springContainer_returnsSameSingletonBeanInstance() {
        SingletonTraceBean first = applicationContext.getBean(SingletonTraceBean.class);
        SingletonTraceBean second = applicationContext.getBean(SingletonTraceBean.class);

        assertThat(first).isSameAs(second);
        assertThat(first.getInstanceId()).isEqualTo(second.getInstanceId());
    }

    @Test
    void inspectSingletonBean_confirmsSameInstanceAndSharedState() {
        SingletonCheckResponse response = singletonContainerService.inspectSingletonBean();

        assertThat(response.isSameInstance()).isTrue();
        assertThat(response.getFirstLookupInstanceId()).isEqualTo(response.getSecondLookupInstanceId());
        assertThat(response.getFirstLookupIdentityHash()).isEqualTo(response.getSecondLookupIdentityHash());
        assertThat(response.getSecondCallCount()).isEqualTo(response.getFirstCallCount() + 1);
    }
}
