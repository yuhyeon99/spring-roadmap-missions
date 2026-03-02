package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton.SingletonTraceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SingletonContainerService {

    private final ApplicationContext applicationContext;

    public SingletonContainerService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public SingletonCheckResponse inspectSingletonBean() {
        SingletonTraceBean firstLookup = applicationContext.getBean(SingletonTraceBean.class);
        SingletonTraceBean secondLookup = applicationContext.getBean(SingletonTraceBean.class);

        int firstCallCount = firstLookup.touch();
        int secondCallCount = secondLookup.touch();

        return new SingletonCheckResponse(
                firstLookup.getInstanceId(),
                secondLookup.getInstanceId(),
                System.identityHashCode(firstLookup),
                System.identityHashCode(secondLookup),
                firstLookup == secondLookup,
                firstCallCount,
                secondCallCount,
                "스프링 컨테이너는 기본 스코프에서 빈을 한 번만 생성하고 재사용합니다."
        );
    }
}
