# 스프링 핵심 원리 - 기본: 순환 의존성 해결하기

이 문서는 `mission-02-spring-core-basic`의 `task-02-circular-dependency` 작업 내용을 정리한 보고서입니다.  
스프링에서 순환 의존성이 발생하는 상황을 테스트로 재현하고, `@Lazy` 프록시를 사용해 실제 애플리케이션 코드에서 순환 참조를 해소했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency`
- 목표:
  - 생성자 주입만 사용할 때 발생하는 순환 의존성 실패 상황을 재현한다.
  - 순환 구조를 유지한 채 `@Lazy`를 적용해 컨테이너 초기화 실패를 해결한다.
  - API 호출과 테스트 실행으로 해결 여부를 확인한다.
- 시나리오:
  - 주문 서비스(`OrderWorkflowService`)와 결제 서비스(`PaymentWorkflowService`)가 서로를 참조하는 구조를 구성한다.
  - 결제 서비스 쪽 의존성에 `@Lazy`를 적용해 초기 생성 시점의 순환 참조를 프록시로 지연 처리한다.

## 2. 구현 단계와 주요 코드

### 2.1 해결된 순환 의존성 구조 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/OrderWorkflowService.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/service/PaymentWorkflowService.java`

`OrderWorkflowService`는 `PaymentWorkflowService`를 생성자 주입받고,  
`PaymentWorkflowService`는 `@Lazy OrderWorkflowService`를 생성자 주입받도록 구현했습니다.

핵심 포인트:
- 순환 참조 자체는 존재하지만, `@Lazy` 프록시가 먼저 주입되어 컨테이너 시작 단계에서 즉시 실체 빈 생성이 일어나지 않습니다.
- 실제 메서드 호출 시점에 대상 빈이 초기화되어 실행됩니다.

### 2.2 확인용 REST API 추가

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/controller/CircularDependencyController.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/dto/CircularDependencyResponse.java`

`GET /mission02/task02/circular-dependency/resolve?orderId=...` 요청으로  
주문/결제 처리 상태와 해결 방식 문자열을 함께 응답하도록 구성했습니다.

### 2.3 실패 재현 + 해결 검증 테스트 작성

- `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task02_circular_dependency/CircularDependencyResolutionTest.java`

테스트 2개를 작성했습니다.
- `circularDependency_withConstructorInjectionOnly_fails`:
  - 테스트 내부의 `BrokenOrderService`/`BrokenPaymentService`를 생성자 주입만으로 등록
  - 컨텍스트 `refresh()` 시 `BeanCurrentlyInCreationException` 루트 원인 발생 확인
- `circularDependency_withLazyProxy_isResolved`:
  - 실제 애플리케이션 빈(`OrderWorkflowService`) 호출
  - 주문/결제 상태 문자열과 해결 메시지 검증

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task02/circular-dependency/resolve?orderId=order-2002"
```

예상 응답:

```json
{
  "orderId": "order-2002",
  "orderState": "ORDER_READY(order-2002)",
  "paymentState": "PAYMENT_READY -> ORDER_READY(order-2002)",
  "resolution": "생성자 주입 + @Lazy 프록시로 순환 의존성을 해소"
}
```

### 3.3 테스트 실행

```bash
./gradlew test --tests "*CircularDependencyResolutionTest"
```

예상 결과:
- 테스트 2건 성공
- 순환 의존성 재현 테스트에서 컨텍스트 초기화 실패가 의도대로 검출됨
- `@Lazy` 적용 구조에서 서비스 호출 결과 검증 성공

## 4. 결과 확인 방법

- `GET /mission02/task02/circular-dependency/resolve` 호출 시 JSON이 정상 응답되는지 확인
- 테스트 리포트에서 `CircularDependencyResolutionTest` 두 케이스 성공 여부 확인
- 실패 재현 테스트 로그에서 `BeanCurrentlyInCreationException` 관련 메시지 확인

## 학습 내용

- 순환 의존성은 두 개 이상의 빈이 서로를 직접 생성자 주입할 때 컨테이너가 어느 빈부터 완성해야 할지 결정하지 못해 초기화가 실패하는 문제입니다.
- 생성자 주입은 의존성을 명확하게 강제하는 장점이 있지만, 구조가 잘못되면 순환 참조가 바로 드러납니다. 이 특성은 설계 문제를 초기에 발견하는 데 유리합니다.
- `@Lazy`는 실제 객체 대신 프록시를 먼저 주입하고, 실제 빈 생성 시점을 뒤로 미뤄 순환 참조 초기화 충돌을 완화합니다.
- 다만 `@Lazy`는 근본적으로 결합을 줄이는 방법은 아니므로, 장기적으로는 공통 책임 분리(중간 서비스 도입 등)로 순환 구조 자체를 제거하는 것이 더 바람직합니다.
