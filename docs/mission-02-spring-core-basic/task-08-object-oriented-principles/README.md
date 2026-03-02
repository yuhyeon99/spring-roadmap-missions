# 스프링 핵심 원리 - 기본: Spring을 통한 객체 지향 원리 적용 실습

이 문서는 `mission-02-spring-core-basic`의 `task-08-object-oriented-principles` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-08-object-oriented-principles`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles`
- 코드 파일 수(테스트 포함): **11개**
- 주요 API 베이스 경로:
  - `/mission02/task08/payments` (PaymentController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/controller/PaymentController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentExecution.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/PaymentResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/dto/ProcessorInfoResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/BankTransferPaymentProcessor.java` | 결제 방식별 다형성 구현 컴포넌트 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/CardPaymentProcessor.java` | 결제 방식별 다형성 구현 컴포넌트 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/KakaoPayPaymentProcessor.java` | 결제 방식별 다형성 구현 컴포넌트 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/PaymentProcessor.java` | 결제 방식별 다형성 구현 컴포넌트 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/service/PaymentService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/PaymentServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `PaymentController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/controller/PaymentController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

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
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

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
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

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
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

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
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

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
- 역할: 결제 방식별 다형성 구현 컴포넌트
- 상세 설명:
- 태스크 동작을 구성하는 보조 요소로, 상위 계층 협력을 돕습니다.

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
- 역할: 결제 방식별 다형성 구현 컴포넌트
- 상세 설명:
- 태스크 동작을 구성하는 보조 요소로, 상위 계층 협력을 돕습니다.

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
- 역할: 결제 방식별 다형성 구현 컴포넌트
- 상세 설명:
- 태스크 동작을 구성하는 보조 요소로, 상위 계층 협력을 돕습니다.

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
- 역할: 결제 방식별 다형성 구현 컴포넌트
- 상세 설명:
- 태스크 동작을 구성하는 보조 요소로, 상위 계층 협력을 돕습니다.

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
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

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
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

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

- **다형성과 OCP**: 인터페이스 기반으로 구현체를 교체/확장하기 쉬운 구조를 만듭니다.  
  참고 문서: https://martinfowler.com/bliki/OCP.html
- **컬렉션 주입(Map/List)**: 동일 인터페이스 구현체를 한 번에 주입받아 런타임 선택 전략을 구현합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task08_object_oriented_principles*"
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
