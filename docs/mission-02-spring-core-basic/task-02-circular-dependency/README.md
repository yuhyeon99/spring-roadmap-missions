# 스프링 핵심 원리 - 기본: 순환 의존성 해결하기

이 문서는 `mission-02-spring-core-basic`의 `task-02-circular-dependency`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-02-circular-dependency`
- 목표:
  - 주문/결제 워크플로우를 분리해 순환 의존 없이 협력 구조를 구성한다.
  - 컨트롤러 호출로 실제 실행 순서와 응답 데이터를 확인한다.
  - 테스트에서 순환 의존 실패 케이스와 해결 구조를 비교 검증한다.
- 엔드포인트: `GET /mission02/task02/circular-dependency/resolve?orderId=...`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/controller/CircularDependencyController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/dto/CircularDependencyResponse.java` | 요청/응답 데이터 구조 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/OrderWorkflowService.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/PaymentWorkflowService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/CircularDependencyResolutionTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. 주문/결제 흐름을 각각 `OrderWorkflowService`, `PaymentWorkflowService`로 분리합니다.
2. 순환 호출이 필요한 지점은 책임을 단방향으로 재배치해 생성 시점 순환 의존을 방지합니다.
3. `CircularDependencyController`에서 `orderId` 기준으로 최종 처리 결과를 조회할 수 있게 구성합니다.
4. 테스트는 해결 구조 정상 동작과, 비교용 실패 구조(내부 정적 클래스) 생성 실패를 함께 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `CircularDependencyController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/controller/CircularDependencyController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission02/task02/circular-dependency`
- 매핑 메서드: Get /resolve;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

<details>
<summary><code>CircularDependencyController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service.OrderWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task02/circular-dependency")
public class CircularDependencyController {

    private final OrderWorkflowService orderWorkflowService;

    public CircularDependencyController(OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    @GetMapping("/resolve")
    public CircularDependencyResponse resolve(@RequestParam(defaultValue = "order-1001") String orderId) {
        return orderWorkflowService.process(orderId);
    }
}
```

</details>

### 4.2 `CircularDependencyResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/dto/CircularDependencyResponse.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>CircularDependencyResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto;

public class CircularDependencyResponse {

    private final String orderId;
    private final String orderState;
    private final String paymentState;
    private final String resolution;

    public CircularDependencyResponse(
            String orderId,
            String orderState,
            String paymentState,
            String resolution
    ) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.paymentState = paymentState;
        this.resolution = resolution;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderState() {
        return orderState;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public String getResolution() {
        return resolution;
    }
}
```

</details>

### 4.3 `OrderWorkflowService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/OrderWorkflowService.java`
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class OrderWorkflowService {,    public OrderWorkflowService(PaymentWorkflowService paymentWorkflowService) {,    public CircularDependencyResponse process(String orderId) {,    public String currentOrderState(String orderId) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

<details>
<summary><code>OrderWorkflowService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import org.springframework.stereotype.Service;

@Service
public class OrderWorkflowService {

    private final PaymentWorkflowService paymentWorkflowService;

    public OrderWorkflowService(PaymentWorkflowService paymentWorkflowService) {
        this.paymentWorkflowService = paymentWorkflowService;
    }

    public CircularDependencyResponse process(String orderId) {
        String orderState = currentOrderState(orderId);
        String paymentState = paymentWorkflowService.prepare(orderId);
        return new CircularDependencyResponse(
                orderId,
                orderState,
                paymentState,
                "생성자 주입 + @Lazy 프록시로 순환 의존성을 해소"
        );
    }

    public String currentOrderState(String orderId) {
        return "ORDER_READY(" + orderId + ")";
    }
}
```

</details>

### 4.4 `PaymentWorkflowService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/PaymentWorkflowService.java`
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class PaymentWorkflowService {,    public PaymentWorkflowService(@Lazy OrderWorkflowService orderWorkflowService) {,    public String prepare(String orderId) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

<details>
<summary><code>PaymentWorkflowService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PaymentWorkflowService {

    private final OrderWorkflowService orderWorkflowService;

    public PaymentWorkflowService(@Lazy OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    public String prepare(String orderId) {
        String orderState = orderWorkflowService.currentOrderState(orderId);
        return "PAYMENT_READY -> " + orderState;
    }
}
```

</details>

### 4.5 `CircularDependencyResolutionTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/CircularDependencyResolutionTest.java`
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `circularDependency_withConstructorInjectionOnly_fails,circularDependency_withLazyProxy_isResolved,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

<details>
<summary><code>CircularDependencyResolutionTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service.OrderWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CircularDependencyResolutionTest {

    @Autowired
    private OrderWorkflowService orderWorkflowService;

    @Test
    void circularDependency_withConstructorInjectionOnly_fails() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BrokenOrderService.class, BrokenPaymentService.class);

        assertThatThrownBy(context::refresh)
                .isInstanceOf(BeanCreationException.class)
                .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);

        context.close();
    }

    @Test
    void circularDependency_withLazyProxy_isResolved() {
        CircularDependencyResponse response = orderWorkflowService.process("order-2002");

        assertThat(response.getOrderId()).isEqualTo("order-2002");
        assertThat(response.getOrderState()).isEqualTo("ORDER_READY(order-2002)");
        assertThat(response.getPaymentState()).isEqualTo("PAYMENT_READY -> ORDER_READY(order-2002)");
        assertThat(response.getResolution()).isEqualTo("생성자 주입 + @Lazy 프록시로 순환 의존성을 해소");
    }

    @Component
    static class BrokenOrderService {
        BrokenOrderService(BrokenPaymentService paymentService) {
        }
    }

    @Component
    static class BrokenPaymentService {
        BrokenPaymentService(BrokenOrderService orderService) {
        }
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **순환 의존성(Circular Dependency)**
  - 핵심: 빈 생성 그래프가 순환하면 컨테이너 초기화가 실패할 수 있습니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- **구조적 해결 방식**
  - 핵심: 책임 재배치/단방향 의존 설계로 근본 원인을 제거합니다.
  - 참고: https://www.baeldung.com/circular-dependencies-in-spring

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task02/circular-dependency/resolve?orderId=order-2002"
```

### 6.3 테스트

```bash
./gradlew test --tests "*task02_circular_dependency*"
```

확인 포인트:
- 해결 구조 테스트 통과
- 순환 구조 비교 테스트에서 예외 메시지 검증

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 순환 의존성은 설정으로 우회하기보다 책임 구조를 재설계하는 것이 장기적으로 안전합니다.
- 테스트에서 실패 구조와 해결 구조를 함께 두면 설계 의사결정 근거가 명확해집니다.
