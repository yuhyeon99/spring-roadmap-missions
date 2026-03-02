# 스프링 핵심 원리 - 기본: 싱글톤 빈 스코프와 프로토타입 빈 스코프 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-07-singleton-prototype-scope` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-07-singleton-prototype-scope`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope`
- 코드 파일 수(테스트 포함): **8개**
- 주요 API 베이스 경로:
  - `/mission02/task07/scopes` (ScopeComparisonController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/controller/ScopeComparisonController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/BeanTouchSnapshot.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/ScopeComparisonResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/ScopePairResult.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/scope/PrototypeScopeBean.java` | 빈 스코프별 인스턴스 동작 정의 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/scope/SingletonScopeBean.java` | 빈 스코프별 인스턴스 동작 정의 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/service/ScopeComparisonService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/ScopeComparisonServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ScopeComparisonController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/controller/ScopeComparisonController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>ScopeComparisonController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopeComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.service.ScopeComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task07/scopes")
public class ScopeComparisonController {

    private final ScopeComparisonService scopeComparisonService;

    public ScopeComparisonController(ScopeComparisonService scopeComparisonService) {
        this.scopeComparisonService = scopeComparisonService;
    }

    @GetMapping
    public ScopeComparisonResponse compare() {
        return scopeComparisonService.compare();
    }
}
```

</details>

### 4.2 `BeanTouchSnapshot.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/BeanTouchSnapshot.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>BeanTouchSnapshot.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class BeanTouchSnapshot {

    private final String instanceId;
    private final int callCount;

    public BeanTouchSnapshot(String instanceId, int callCount) {
        this.instanceId = instanceId;
        this.callCount = callCount;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getCallCount() {
        return callCount;
    }
}
```

</details>

### 4.3 `ScopeComparisonResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/ScopeComparisonResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>ScopeComparisonResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class ScopeComparisonResponse {

    private final ScopePairResult singletonScope;
    private final ScopePairResult prototypeInjectedIntoSingleton;
    private final ScopePairResult prototypeFromProvider;

    public ScopeComparisonResponse(
            ScopePairResult singletonScope,
            ScopePairResult prototypeInjectedIntoSingleton,
            ScopePairResult prototypeFromProvider
    ) {
        this.singletonScope = singletonScope;
        this.prototypeInjectedIntoSingleton = prototypeInjectedIntoSingleton;
        this.prototypeFromProvider = prototypeFromProvider;
    }

    public ScopePairResult getSingletonScope() {
        return singletonScope;
    }

    public ScopePairResult getPrototypeInjectedIntoSingleton() {
        return prototypeInjectedIntoSingleton;
    }

    public ScopePairResult getPrototypeFromProvider() {
        return prototypeFromProvider;
    }
}
```

</details>

### 4.4 `ScopePairResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/dto/ScopePairResult.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>ScopePairResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class ScopePairResult {

    private final String firstInstanceId;
    private final int firstCallCount;
    private final String secondInstanceId;
    private final int secondCallCount;
    private final boolean sameInstance;
    private final String explanation;

    public ScopePairResult(
            String firstInstanceId,
            int firstCallCount,
            String secondInstanceId,
            int secondCallCount,
            boolean sameInstance,
            String explanation
    ) {
        this.firstInstanceId = firstInstanceId;
        this.firstCallCount = firstCallCount;
        this.secondInstanceId = secondInstanceId;
        this.secondCallCount = secondCallCount;
        this.sameInstance = sameInstance;
        this.explanation = explanation;
    }

    public String getFirstInstanceId() {
        return firstInstanceId;
    }

    public int getFirstCallCount() {
        return firstCallCount;
    }

    public String getSecondInstanceId() {
        return secondInstanceId;
    }

    public int getSecondCallCount() {
        return secondCallCount;
    }

    public boolean isSameInstance() {
        return sameInstance;
    }

    public String getExplanation() {
        return explanation;
    }
}
```

</details>

### 4.5 `PrototypeScopeBean.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/scope/PrototypeScopeBean.java`
- 역할: 빈 스코프별 인스턴스 동작 정의
- 상세 설명:
- 스코프에 따라 빈 생성/재사용 시점이 어떻게 달라지는지 실습합니다.
- 동일 빈 재사용 여부를 상태값으로 추적해 동작 차이를 검증합니다.

<details>
<summary><code>PrototypeScopeBean.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.scope;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.BeanTouchSnapshot;
import java.util.UUID;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeScopeBean {

    private final String instanceId = UUID.randomUUID().toString();
    private int callCount;

    public BeanTouchSnapshot touch() {
        callCount += 1;
        return new BeanTouchSnapshot(instanceId, callCount);
    }
}
```

</details>

### 4.6 `SingletonScopeBean.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/scope/SingletonScopeBean.java`
- 역할: 빈 스코프별 인스턴스 동작 정의
- 상세 설명:
- 스코프에 따라 빈 생성/재사용 시점이 어떻게 달라지는지 실습합니다.
- 동일 빈 재사용 여부를 상태값으로 추적해 동작 차이를 검증합니다.

<details>
<summary><code>SingletonScopeBean.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.7 `ScopeComparisonService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/service/ScopeComparisonService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>ScopeComparisonService.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.8 `ScopeComparisonServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task07_singleton_prototype_scope/ScopeComparisonServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>ScopeComparisonServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **빈 스코프**: 싱글톤/프로토타입에 따라 생성과 재사용 시점이 달라집니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html
- **`ObjectProvider` 지연 조회**: 필요 시점마다 새로운 프로토타입 빈을 조회할 수 있습니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-method-injection.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task07_singleton_prototype_scope*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 현재 태스크 디렉토리의 스크린샷 파일:
  - `scope-run-result.png`

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
