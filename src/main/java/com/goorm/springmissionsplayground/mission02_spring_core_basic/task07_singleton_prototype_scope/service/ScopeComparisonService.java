package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.BeanTouchSnapshot;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopeComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopePairResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.scope.PrototypeScopeBean;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.scope.SingletonScopeBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class ScopeComparisonService {

    private final SingletonScopeBean singletonScopeBean;
    private final PrototypeScopeBean injectedPrototypeScopeBean;
    private final ObjectProvider<PrototypeScopeBean> prototypeScopeBeanProvider;

    public ScopeComparisonService(
            SingletonScopeBean singletonScopeBean,
            PrototypeScopeBean injectedPrototypeScopeBean,
            ObjectProvider<PrototypeScopeBean> prototypeScopeBeanProvider
    ) {
        this.singletonScopeBean = singletonScopeBean;
        this.injectedPrototypeScopeBean = injectedPrototypeScopeBean;
        this.prototypeScopeBeanProvider = prototypeScopeBeanProvider;
    }

    public ScopeComparisonResponse compare() {
        ScopePairResult singletonResult = toPairResult(
                singletonScopeBean.touch(),
                singletonScopeBean.touch(),
                "싱글톤 스코프: 컨테이너에 하나의 인스턴스가 유지되어 호출할수록 상태가 누적됩니다."
        );

        ScopePairResult injectedPrototypeResult = toPairResult(
                injectedPrototypeScopeBean.touch(),
                injectedPrototypeScopeBean.touch(),
                "프로토타입 빈을 싱글톤에 직접 주입하면 생성 시점 1회만 주입되어 같은 인스턴스를 계속 사용합니다."
        );

        ScopePairResult providerPrototypeResult = toPairResult(
                prototypeScopeBeanProvider.getObject().touch(),
                prototypeScopeBeanProvider.getObject().touch(),
                "ObjectProvider로 조회하면 요청할 때마다 새 프로토타입 인스턴스를 받습니다."
        );

        return new ScopeComparisonResponse(
                singletonResult,
                injectedPrototypeResult,
                providerPrototypeResult
        );
    }

    private ScopePairResult toPairResult(
            BeanTouchSnapshot first,
            BeanTouchSnapshot second,
            String explanation
    ) {
        return new ScopePairResult(
                first.getInstanceId(),
                first.getCallCount(),
                second.getInstanceId(),
                second.getCallCount(),
                first.getInstanceId().equals(second.getInstanceId()),
                explanation
        );
    }
}
