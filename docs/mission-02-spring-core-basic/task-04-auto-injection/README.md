# 스프링 핵심 원리 - 기본: 의존관계 자동 주입 방식 실습

이 문서는 `mission-02-spring-core-basic`의 `task-04-auto-injection` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-04-auto-injection`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection`
- 코드 파일 수(테스트 포함): **12개**
- 주요 API 베이스 경로:
  - `/mission02/task04/auto-injection` (AutoInjectionController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/controller/AutoInjectionController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/AutoInjectionComparisonResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/InjectionCaseResult.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/DiscountPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/FixedDiscountPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/RateDiscountPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AmountFormatter.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutoInjectionComparisonService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutowiredOnlyService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/PrimaryInjectionService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/QualifierInjectionService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/AutoInjectionComparisonServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `AutoInjectionController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/controller/AutoInjectionController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>AutoInjectionController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service.AutoInjectionComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task04/auto-injection")
public class AutoInjectionController {

    private final AutoInjectionComparisonService autoInjectionComparisonService;

    public AutoInjectionController(AutoInjectionComparisonService autoInjectionComparisonService) {
        this.autoInjectionComparisonService = autoInjectionComparisonService;
    }

    @GetMapping
    public AutoInjectionComparisonResponse compare(@RequestParam(defaultValue = "20000") int amount) {
        return autoInjectionComparisonService.compare(amount);
    }
}
```

</details>

### 4.2 `AutoInjectionComparisonResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/AutoInjectionComparisonResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>AutoInjectionComparisonResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto;

import java.util.List;

public class AutoInjectionComparisonResponse {

    private final int amount;
    private final List<InjectionCaseResult> comparisons;

    public AutoInjectionComparisonResponse(int amount, List<InjectionCaseResult> comparisons) {
        this.amount = amount;
        this.comparisons = comparisons;
    }

    public int getAmount() {
        return amount;
    }

    public List<InjectionCaseResult> getComparisons() {
        return comparisons;
    }
}
```

</details>

### 4.3 `InjectionCaseResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/InjectionCaseResult.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>InjectionCaseResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto;

public class InjectionCaseResult {

    private final String injectionType;
    private final String injectedBean;
    private final String result;
    private final String reason;

    public InjectionCaseResult(
            String injectionType,
            String injectedBean,
            String result,
            String reason
    ) {
        this.injectionType = injectionType;
        this.injectedBean = injectedBean;
        this.result = result;
        this.reason = reason;
    }

    public String getInjectionType() {
        return injectionType;
    }

    public String getInjectedBean() {
        return injectedBean;
    }

    public String getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }
}
```

</details>

### 4.4 `DiscountPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/DiscountPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>DiscountPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

public interface DiscountPolicy {

    int discount(int amount);

    String beanName();
}
```

</details>

### 4.5 `FixedDiscountPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/FixedDiscountPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>FixedDiscountPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

import org.springframework.stereotype.Component;

@Component("fixedDiscountPolicy")
public class FixedDiscountPolicy implements DiscountPolicy {

    private static final int FIXED_DISCOUNT_AMOUNT = 1000;

    @Override
    public int discount(int amount) {
        return Math.min(amount, FIXED_DISCOUNT_AMOUNT);
    }

    @Override
    public String beanName() {
        return "fixedDiscountPolicy";
    }
}
```

</details>

### 4.6 `RateDiscountPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/RateDiscountPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>RateDiscountPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("rateDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {

    private static final int DISCOUNT_RATE = 10;

    @Override
    public int discount(int amount) {
        return amount * DISCOUNT_RATE / 100;
    }

    @Override
    public String beanName() {
        return "rateDiscountPolicy";
    }
}
```

</details>

### 4.7 `AmountFormatter.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AmountFormatter.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>AmountFormatter.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class AmountFormatter {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);

    public String format(int amount) {
        return numberFormat.format(amount) + "원";
    }
}
```

</details>

### 4.8 `AutoInjectionComparisonService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutoInjectionComparisonService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>AutoInjectionComparisonService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AutoInjectionComparisonService {

    private final AutowiredOnlyService autowiredOnlyService;
    private final QualifierInjectionService qualifierInjectionService;
    private final PrimaryInjectionService primaryInjectionService;

    public AutoInjectionComparisonService(
            AutowiredOnlyService autowiredOnlyService,
            QualifierInjectionService qualifierInjectionService,
            PrimaryInjectionService primaryInjectionService
    ) {
        this.autowiredOnlyService = autowiredOnlyService;
        this.qualifierInjectionService = qualifierInjectionService;
        this.primaryInjectionService = primaryInjectionService;
    }

    public AutoInjectionComparisonResponse compare(int amount) {
        List<InjectionCaseResult> comparisons = List.of(
                autowiredOnlyService.compare(amount),
                qualifierInjectionService.compare(amount),
                primaryInjectionService.compare(amount)
        );
        return new AutoInjectionComparisonResponse(amount, comparisons);
    }
}
```

</details>

### 4.9 `AutowiredOnlyService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutowiredOnlyService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>AutowiredOnlyService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutowiredOnlyService {

    private final AmountFormatter amountFormatter;

    @Autowired
    public AutowiredOnlyService(AmountFormatter amountFormatter) {
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        return new InjectionCaseResult(
                "@Autowired",
                "amountFormatter",
                "포맷 결과: " + amountFormatter.format(amount),
                "동일 타입 빈이 1개라서 타입 기반 자동 주입"
        );
    }
}
```

</details>

### 4.10 `PrimaryInjectionService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/PrimaryInjectionService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>PrimaryInjectionService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy.DiscountPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrimaryInjectionService {

    private final DiscountPolicy discountPolicy;
    private final AmountFormatter amountFormatter;

    @Autowired
    public PrimaryInjectionService(DiscountPolicy discountPolicy, AmountFormatter amountFormatter) {
        this.discountPolicy = discountPolicy;
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        int discounted = discountPolicy.discount(amount);
        return new InjectionCaseResult(
                "@Autowired + @Primary",
                discountPolicy.beanName(),
                "할인 금액: " + amountFormatter.format(discounted),
                "@Primary가 지정된 빈을 기본 후보로 우선 선택"
        );
    }
}
```

</details>

### 4.11 `QualifierInjectionService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/QualifierInjectionService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>QualifierInjectionService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy.DiscountPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QualifierInjectionService {

    private final DiscountPolicy discountPolicy;
    private final AmountFormatter amountFormatter;

    @Autowired
    public QualifierInjectionService(
            @Qualifier("fixedDiscountPolicy") DiscountPolicy discountPolicy,
            AmountFormatter amountFormatter
    ) {
        this.discountPolicy = discountPolicy;
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        int discounted = discountPolicy.discount(amount);
        return new InjectionCaseResult(
                "@Autowired + @Qualifier",
                discountPolicy.beanName(),
                "할인 금액: " + amountFormatter.format(discounted),
                "동일 타입 빈이 여러 개일 때 이름으로 주입 대상을 지정"
        );
    }
}
```

</details>

### 4.12 `AutoInjectionComparisonServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/AutoInjectionComparisonServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>AutoInjectionComparisonServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service.AutoInjectionComparisonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutoInjectionComparisonServiceTest {

    @Autowired
    private AutoInjectionComparisonService autoInjectionComparisonService;

    @Test
    void compare_returnsDifferentResultsByInjectionStrategy() {
        AutoInjectionComparisonResponse response = autoInjectionComparisonService.compare(20000);

        assertThat(response.getAmount()).isEqualTo(20000);
        assertThat(response.getComparisons()).hasSize(3);

        InjectionCaseResult autowired = response.getComparisons().get(0);
        InjectionCaseResult qualifier = response.getComparisons().get(1);
        InjectionCaseResult primary = response.getComparisons().get(2);

        assertThat(autowired.getInjectionType()).isEqualTo("@Autowired");
        assertThat(autowired.getInjectedBean()).isEqualTo("amountFormatter");
        assertThat(autowired.getResult()).isEqualTo("포맷 결과: 20,000원");

        assertThat(qualifier.getInjectionType()).isEqualTo("@Autowired + @Qualifier");
        assertThat(qualifier.getInjectedBean()).isEqualTo("fixedDiscountPolicy");
        assertThat(qualifier.getResult()).isEqualTo("할인 금액: 1,000원");

        assertThat(primary.getInjectionType()).isEqualTo("@Autowired + @Primary");
        assertThat(primary.getInjectedBean()).isEqualTo("rateDiscountPolicy");
        assertThat(primary.getResult()).isEqualTo("할인 금액: 2,000원");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **`@Autowired` 자동 주입**: 타입 기반으로 의존성을 주입합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html
- **`@Qualifier` / `@Primary`**: 다중 구현체에서 우선순위 또는 이름으로 후보를 선택합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task04_auto_injection*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 현재 태스크 디렉토리의 스크린샷 파일:
  - `code-autowired.png`
  - `code-primary.png`
  - `code-qualifier.png`
  - `injection-comparison-table.png`

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
