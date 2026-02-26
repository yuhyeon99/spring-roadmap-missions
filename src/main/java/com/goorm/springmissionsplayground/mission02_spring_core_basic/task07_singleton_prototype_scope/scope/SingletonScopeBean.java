package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.scope;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.BeanTouchSnapshot;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SingletonScopeBean {

    private final String instanceId = UUID.randomUUID().toString();
    private int callCount;

    public BeanTouchSnapshot touch() {
        callCount += 1;
        return new BeanTouchSnapshot(instanceId, callCount);
    }
}
