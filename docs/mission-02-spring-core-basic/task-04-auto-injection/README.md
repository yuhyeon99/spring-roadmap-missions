# 스프링 핵심 원리 - 기본: 의존관계 자동 주입 방식 실습

이 문서는 `mission-02-spring-core-basic`의 `task-04-auto-injection`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-04-auto-injection`
- 목표:
  - `@Autowired`, `@Qualifier`, `@Primary` 세 가지 자동 주입 방식을 비교한다.
  - 동일 인터페이스(`DiscountPolicy`) 다중 구현체 환경에서 선택 규칙을 검증한다.
  - 하나의 API 호출에서 방식별 할인 결과를 동시에 반환해 차이를 관찰한다.
- 엔드포인트: `GET /mission02/task04/auto-injection?amount=...`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/controller/AutoInjectionController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/AutoInjectionComparisonResponse.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/dto/InjectionCaseResult.java` | 요청/응답 데이터 구조 |
| Policy | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/DiscountPolicy.java` | 전략 인터페이스/구현체 |
| Policy | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/FixedDiscountPolicy.java` | 전략 인터페이스/구현체 |
| Policy | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/policy/RateDiscountPolicy.java` | 전략 인터페이스/구현체 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AmountFormatter.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutoInjectionComparisonService.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/AutowiredOnlyService.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/PrimaryInjectionService.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/service/QualifierInjectionService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/AutoInjectionComparisonServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `DiscountPolicy` 구현체 2종(`FixedDiscountPolicy`, `RateDiscountPolicy`)을 빈으로 등록합니다.
2. `AutowiredOnlyService`, `QualifierInjectionService`, `PrimaryInjectionService`를 각각 다른 주입 전략으로 구성합니다.
3. `AutoInjectionComparisonService`가 세 서비스 결과를 하나의 응답으로 합쳐 비교 가능하게 만듭니다.
4. `AutoInjectionController`는 입력 금액 기준으로 주입 방식별 할인 결과를 반환합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `AutoInjectionController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task04_auto_injection/controller/AutoInjectionController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission02/task04/auto-injection`
- 매핑 메서드: Get;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 전략 인터페이스/구현체
- 상세 설명:
- 공통 인터페이스 아래 구현체를 분리해 전략 교체가 가능한 구조를 만듭니다.
- 클라이언트는 구체 구현이 아닌 추상 타입에 의존합니다.
- 요구사항 추가 시 새 정책 클래스를 추가하는 방식으로 확장합니다.

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
- 역할: 전략 인터페이스/구현체
- 상세 설명:
- 공통 인터페이스 아래 구현체를 분리해 전략 교체가 가능한 구조를 만듭니다.
- 클라이언트는 구체 구현이 아닌 추상 타입에 의존합니다.
- 요구사항 추가 시 새 정책 클래스를 추가하는 방식으로 확장합니다.

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
- 역할: 전략 인터페이스/구현체
- 상세 설명:
- 공통 인터페이스 아래 구현체를 분리해 전략 교체가 가능한 구조를 만듭니다.
- 클라이언트는 구체 구현이 아닌 추상 타입에 의존합니다.
- 요구사항 추가 시 새 정책 클래스를 추가하는 방식으로 확장합니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class AmountFormatter {,    public String format(int amount) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class AutoInjectionComparisonService {,    public AutoInjectionComparisonService(,    public AutoInjectionComparisonResponse compare(int amount) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class AutowiredOnlyService {,    public AutowiredOnlyService(AmountFormatter amountFormatter) {,    public InjectionCaseResult compare(int amount) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class PrimaryInjectionService {,    public PrimaryInjectionService(DiscountPolicy discountPolicy, AmountFormatter amountFormatter) {,    public InjectionCaseResult compare(int amount) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class QualifierInjectionService {,    public QualifierInjectionService(,    public InjectionCaseResult compare(int amount) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `compare_returnsDifferentResultsByInjectionStrategy,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

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

- **`@Autowired` 타입 기반 주입**
  - 핵심: 타입 일치 빈을 자동으로 연결합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html
- **`@Qualifier` / `@Primary` 선택 규칙**
  - 핵심: 다중 후보에서 이름 우선 또는 기본 후보를 명시할 수 있습니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task04/auto-injection?amount=20000"
```

확인 포인트:
- 3가지 주입 방식 결과가 한 응답에 모두 포함되는지
- 각 방식별 선택된 정책/할인 금액이 기대와 일치하는지

### 6.3 테스트

```bash
./gradlew test --tests "*task04_auto_injection*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 자동 주입은 편리하지만 다중 빈 환경에서는 선택 규칙(`@Qualifier`, `@Primary`)을 명시해야 예측 가능합니다.
- 비교 API를 만들면 주입 방식별 차이를 팀원과 공유하기 쉬워집니다.
