# 스프링 핵심 원리 - 기본: 싱글톤 컨테이너 원리 이해하기

이 문서는 `mission-02-spring-core-basic`의 `task-10-singleton-container`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-10-singleton-container`
- 목표:
  - 컨테이너에서 같은 타입 빈을 두 번 조회해 동일 인스턴스인지 확인한다.
  - `instanceId`, `identityHashCode`, `callCount`로 싱글톤 재사용과 상태 공유를 검증한다.
  - 테스트/문서로 싱글톤 장단점을 실습 결과와 연결해 정리한다.
- 엔드포인트: `GET /mission02/task10/singleton/inspect`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/controller/SingletonContainerController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/dto/SingletonCheckResponse.java` | 요청/응답 데이터 구조 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/service/SingletonContainerService.java` | 비즈니스 로직과 흐름 제어 |
| Singleton | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/singleton/SingletonTraceBean.java` | 싱글톤 동작 추적 컴포넌트 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/SingletonContainerServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `SingletonTraceBean`에 `instanceId`와 `callCount`를 두어 동일 인스턴스/상태 공유 여부를 확인합니다.
2. `SingletonContainerService`가 `ApplicationContext#getBean()`을 두 번 호출해 참조 동일성(`==`)과 해시를 비교합니다.
3. 컨트롤러는 비교 결과를 응답 DTO(`SingletonCheckResponse`)로 반환해 즉시 관찰 가능하게 합니다.
4. 테스트는 동일 참조, 동일 instanceId, 증가하는 callCount를 기준으로 싱글톤 동작을 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `SingletonContainerController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/controller/SingletonContainerController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission02/task10/singleton`
- 매핑 메서드: Get /inspect;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

<details>
<summary><code>SingletonContainerController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service.SingletonContainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task10/singleton")
public class SingletonContainerController {

    private final SingletonContainerService singletonContainerService;

    public SingletonContainerController(SingletonContainerService singletonContainerService) {
        this.singletonContainerService = singletonContainerService;
    }

    @GetMapping("/inspect")
    public SingletonCheckResponse inspect() {
        return singletonContainerService.inspectSingletonBean();
    }
}
```

</details>

### 4.2 `SingletonCheckResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/dto/SingletonCheckResponse.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>SingletonCheckResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto;

public class SingletonCheckResponse {

    private final String firstLookupInstanceId;
    private final String secondLookupInstanceId;
    private final int firstLookupIdentityHash;
    private final int secondLookupIdentityHash;
    private final boolean sameInstance;
    private final int firstCallCount;
    private final int secondCallCount;
    private final String summary;

    public SingletonCheckResponse(
            String firstLookupInstanceId,
            String secondLookupInstanceId,
            int firstLookupIdentityHash,
            int secondLookupIdentityHash,
            boolean sameInstance,
            int firstCallCount,
            int secondCallCount,
            String summary
    ) {
        this.firstLookupInstanceId = firstLookupInstanceId;
        this.secondLookupInstanceId = secondLookupInstanceId;
        this.firstLookupIdentityHash = firstLookupIdentityHash;
        this.secondLookupIdentityHash = secondLookupIdentityHash;
        this.sameInstance = sameInstance;
        this.firstCallCount = firstCallCount;
        this.secondCallCount = secondCallCount;
        this.summary = summary;
    }

    public String getFirstLookupInstanceId() {
        return firstLookupInstanceId;
    }

    public String getSecondLookupInstanceId() {
        return secondLookupInstanceId;
    }

    public int getFirstLookupIdentityHash() {
        return firstLookupIdentityHash;
    }

    public int getSecondLookupIdentityHash() {
        return secondLookupIdentityHash;
    }

    public boolean isSameInstance() {
        return sameInstance;
    }

    public int getFirstCallCount() {
        return firstCallCount;
    }

    public int getSecondCallCount() {
        return secondCallCount;
    }

    public String getSummary() {
        return summary;
    }
}
```

</details>

### 4.3 `SingletonContainerService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/service/SingletonContainerService.java`
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class SingletonContainerService {,    public SingletonContainerService(ApplicationContext applicationContext) {,    public SingletonCheckResponse inspectSingletonBean() {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

<details>
<summary><code>SingletonContainerService.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.4 `SingletonTraceBean.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/singleton/SingletonTraceBean.java`
- 역할: 싱글톤 동작 추적 컴포넌트
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

<details>
<summary><code>SingletonTraceBean.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SingletonTraceBean {

    private final String instanceId = UUID.randomUUID().toString();
    private int callCount = 0;

    public synchronized int touch() {
        callCount++;
        return callCount;
    }

    public synchronized int getCallCount() {
        return callCount;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
```

</details>

### 4.5 `SingletonContainerServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/SingletonContainerServiceTest.java`
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `springContainer_returnsSameSingletonBeanInstance,inspectSingletonBean_confirmsSameInstanceAndSharedState,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

<details>
<summary><code>SingletonContainerServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **싱글톤 컨테이너 원리**
  - 핵심: 기본 스코프에서 빈을 1회 생성 후 재사용합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html
- **상태 공유와 동시성 주의**
  - 핵심: 싱글톤은 상태를 공유하므로 무상태 설계가 중요합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task10/singleton/inspect"
```

확인 포인트:
- `sameInstance = true`
- `firstLookupInstanceId == secondLookupInstanceId`
- `secondCallCount = firstCallCount + 1`

### 6.3 테스트

```bash
./gradlew test --tests "*task10_singleton_container*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 싱글톤은 동일 참조 재사용뿐 아니라 상태 공유까지 동반된다는 점을 실험으로 확인했습니다.
- 실무에서는 싱글톤 빈을 무상태로 유지해야 동시성 문제를 예방할 수 있습니다.
