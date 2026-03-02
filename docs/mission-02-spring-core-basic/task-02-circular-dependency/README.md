# 스프링 핵심 원리 - 기본: 순환 의존성 해결하기

이 문서는 `mission-02-spring-core-basic`의 `task-02-circular-dependency` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-02-circular-dependency`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency`
- 코드 파일 수(테스트 포함): **5개**
- 주요 API 베이스 경로:
  - `/mission02/task02/circular-dependency` (CircularDependencyController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/controller/CircularDependencyController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/dto/CircularDependencyResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/OrderWorkflowService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/PaymentWorkflowService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/CircularDependencyResolutionTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `CircularDependencyController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/controller/CircularDependencyController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

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
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

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
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

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
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

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
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

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

- **순환 의존성**: 빈 A와 B가 서로를 필요로 할 때 생성 순환 문제가 발생할 수 있습니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- **지연 주입/구조 분리**: 책임 재배치로 순환을 끊고 단방향 의존으로 개선합니다.  
  참고 문서: https://www.baeldung.com/circular-dependencies-in-spring

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task02_circular_dependency*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 필요 시 실행 결과를 캡처해 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
