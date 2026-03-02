# 스프링 핵심 원리 - 기본: Spring을 통한 객체 지향 원리 적용 실습

이 문서는 `mission-02-spring-core-basic`의 `task-08-object-oriented-principles`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-08-object-oriented-principles`
- 목표:
  - `PaymentProcessor` 인터페이스와 구현체 3종(카드/카카오페이/계좌이체)으로 다형성 구조를 만든다.
  - 스프링이 주입한 `Map<String, PaymentProcessor>`를 이용해 런타임 방식 선택을 구현한다.
  - OCP 관점에서 결제 수단 확장 가능 구조를 검증한다.
- 엔드포인트: `GET /mission02/task08/payments/processors`, `POST /mission02/task08/payments`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/controller/PaymentController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentExecution.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentRequest.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentResponse.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/ProcessorInfoResponse.java` | 요청/응답 데이터 구조 |
| Payment | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/BankTransferPaymentProcessor.java` | 결제 방식 다형성 구현 |
| Payment | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/CardPaymentProcessor.java` | 결제 방식 다형성 구현 |
| Payment | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/KakaoPayPaymentProcessor.java` | 결제 방식 다형성 구현 |
| Payment | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/PaymentProcessor.java` | 결제 방식 다형성 구현 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/service/PaymentService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/PaymentServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `PaymentProcessor` 인터페이스로 결제 행위를 추상화하고 구현체별 수수료 정책을 분리합니다.
2. `PaymentService`는 `Map<String, PaymentProcessor>` 주입으로 결제 방식 문자열을 구현체 선택 로직에 연결합니다.
3. `processorInfo()` API로 방식→빈 이름 매핑을 노출해 주입 결과를 관찰할 수 있게 합니다.
4. `pay()`에서는 금액/방식 검증 후 선택된 구현체를 실행해 응답 DTO를 구성합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `PaymentController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/controller/PaymentController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission02/task08/payments`
- 매핑 메서드: Get /processors;Post;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

<details>
<summary><code>PaymentController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task08/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/processors")
    public ProcessorInfoResponse processors() {
        return paymentService.processorInfo();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return paymentService.pay(request);
    }
}
```

</details>

### 4.2 `PaymentExecution.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentExecution.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>PaymentExecution.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentExecution {

    private final int fee;
    private final int approvedAmount;
    private final String detail;

    public PaymentExecution(int fee, int approvedAmount, String detail) {
        this.fee = fee;
        this.approvedAmount = approvedAmount;
        this.detail = detail;
    }

    public int getFee() {
        return fee;
    }

    public int getApprovedAmount() {
        return approvedAmount;
    }

    public String getDetail() {
        return detail;
    }
}
```

</details>

### 4.3 `PaymentRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentRequest.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>PaymentRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentRequest {

    private String orderId;
    private int amount;
    private String method;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
```

</details>

### 4.4 `PaymentResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentResponse.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>PaymentResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentResponse {

    private final String orderId;
    private final String method;
    private final String processorBean;
    private final int requestedAmount;
    private final int fee;
    private final int approvedAmount;
    private final String message;

    public PaymentResponse(
            String orderId,
            String method,
            String processorBean,
            int requestedAmount,
            int fee,
            int approvedAmount,
            String message
    ) {
        this.orderId = orderId;
        this.method = method;
        this.processorBean = processorBean;
        this.requestedAmount = requestedAmount;
        this.fee = fee;
        this.approvedAmount = approvedAmount;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMethod() {
        return method;
    }

    public String getProcessorBean() {
        return processorBean;
    }

    public int getRequestedAmount() {
        return requestedAmount;
    }

    public int getFee() {
        return fee;
    }

    public int getApprovedAmount() {
        return approvedAmount;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.5 `ProcessorInfoResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/ProcessorInfoResponse.java`
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

<details>
<summary><code>ProcessorInfoResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

import java.util.Map;

public class ProcessorInfoResponse {

    private final Map<String, String> methodToBean;
    private final String polymorphismSummary;

    public ProcessorInfoResponse(Map<String, String> methodToBean, String polymorphismSummary) {
        this.methodToBean = methodToBean;
        this.polymorphismSummary = polymorphismSummary;
    }

    public Map<String, String> getMethodToBean() {
        return methodToBean;
    }

    public String getPolymorphismSummary() {
        return polymorphismSummary;
    }
}
```

</details>

### 4.6 `BankTransferPaymentProcessor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/BankTransferPaymentProcessor.java`
- 역할: 결제 방식 다형성 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

<details>
<summary><code>BankTransferPaymentProcessor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("bankTransferPaymentProcessor")
public class BankTransferPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "bank";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = Math.min(amount, 500);
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "계좌이체 고정 수수료 500원 적용");
    }
}
```

</details>

### 4.7 `CardPaymentProcessor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/CardPaymentProcessor.java`
- 역할: 결제 방식 다형성 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

<details>
<summary><code>CardPaymentProcessor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("cardPaymentProcessor")
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "card";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = amount * 3 / 100;
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "카드 결제 수수료 3% 적용");
    }
}
```

</details>

### 4.8 `KakaoPayPaymentProcessor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/KakaoPayPaymentProcessor.java`
- 역할: 결제 방식 다형성 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

<details>
<summary><code>KakaoPayPaymentProcessor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("kakaoPayPaymentProcessor")
public class KakaoPayPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "kakaopay";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = amount * 2 / 100;
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "카카오페이 결제 수수료 2% 적용");
    }
}
```

</details>

### 4.9 `PaymentProcessor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/PaymentProcessor.java`
- 역할: 결제 방식 다형성 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

<details>
<summary><code>PaymentProcessor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;

public interface PaymentProcessor {

    String methodKey();

    PaymentExecution pay(int amount);
}
```

</details>

### 4.10 `PaymentService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/service/PaymentService.java`
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class PaymentService {,    public PaymentService(Map<String, PaymentProcessor> paymentProcessorBeans) {,    public PaymentResponse pay(PaymentRequest request) {,    public ProcessorInfoResponse processorInfo() {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

<details>
<summary><code>PaymentService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment.PaymentProcessor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaymentService {

    private final Map<String, PaymentProcessor> processorByMethod = new LinkedHashMap<>();
    private final Map<String, String> beanNameByMethod = new LinkedHashMap<>();

    public PaymentService(Map<String, PaymentProcessor> paymentProcessorBeans) {
        paymentProcessorBeans.forEach((beanName, processor) -> {
            String method = processor.methodKey();
            processorByMethod.put(method, processor);
            beanNameByMethod.put(method, beanName);
        });
    }

    public PaymentResponse pay(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }

        String method = normalizeMethod(request.getMethod());
        PaymentProcessor paymentProcessor = processorByMethod.get(method);
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 방식입니다. method=" + method);
        }

        PaymentExecution execution = paymentProcessor.pay(request.getAmount());
        return new PaymentResponse(
                request.getOrderId(),
                method,
                beanNameByMethod.get(method),
                request.getAmount(),
                execution.getFee(),
                execution.getApprovedAmount(),
                execution.getDetail()
        );
    }

    public ProcessorInfoResponse processorInfo() {
        return new ProcessorInfoResponse(
                new LinkedHashMap<>(beanNameByMethod),
                "스프링 컨테이너가 PaymentProcessor 구현체들을 모두 빈으로 등록하고, 요청 결제 타입에 맞는 구현체를 런타임에 선택합니다."
        );
    }

    private String normalizeMethod(String method) {
        if (!StringUtils.hasText(method)) {
            throw new IllegalArgumentException("method는 필수입니다.");
        }
        return method.trim().toLowerCase(Locale.ROOT);
    }
}
```

</details>

### 4.11 `PaymentServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/PaymentServiceTest.java`
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `processorInfo_containsAllPaymentImplementations,pay_selectsImplementationByMethod_usingPolymorphism,pay_throwsWhenMethodIsUnsupported,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

<details>
<summary><code>PaymentServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void processorInfo_containsAllPaymentImplementations() {
        ProcessorInfoResponse info = paymentService.processorInfo();

        assertThat(info.getMethodToBean()).hasSize(3);
        assertThat(info.getMethodToBean())
                .containsEntry("card", "cardPaymentProcessor")
                .containsEntry("kakaopay", "kakaoPayPaymentProcessor")
                .containsEntry("bank", "bankTransferPaymentProcessor");
    }

    @Test
    void pay_selectsImplementationByMethod_usingPolymorphism() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order-8001");
        request.setAmount(10000);
        request.setMethod("kakaopay");

        PaymentResponse response = paymentService.pay(request);

        assertThat(response.getProcessorBean()).isEqualTo("kakaoPayPaymentProcessor");
        assertThat(response.getFee()).isEqualTo(200);
        assertThat(response.getApprovedAmount()).isEqualTo(9800);
        assertThat(response.getMessage()).contains("2%");
    }

    @Test
    void pay_throwsWhenMethodIsUnsupported() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order-8002");
        request.setAmount(10000);
        request.setMethod("crypto");

        assertThatThrownBy(() -> paymentService.pay(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 결제 방식");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **다형성 + OCP**
  - 핵심: 인터페이스 기반 설계로 기능 확장 시 기존 코드를 덜 수정합니다.
  - 참고: https://martinfowler.com/bliki/OCP.html
- **컬렉션 주입(Map Injection)**
  - 핵심: 구현체 집합을 주입받아 런타임 전략 선택을 구현합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl http://localhost:8080/mission02/task08/payments/processors

curl -X POST http://localhost:8080/mission02/task08/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"order-8001","amount":10000,"method":"kakaopay"}'
```

### 6.3 테스트

```bash
./gradlew test --tests "*task08_object_oriented_principles*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 다형성 구조와 맵 주입 조합은 런타임 전략 선택이 필요한 도메인에 특히 효과적입니다.
- 인터페이스 중심 설계는 신규 결제 수단 추가 시 기존 코드 수정 범위를 최소화합니다.
