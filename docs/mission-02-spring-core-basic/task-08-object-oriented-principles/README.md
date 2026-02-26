# 스프링 핵심 원리 - 기본: Spring을 통한 객체 지향 원리 적용 실습

이 문서는 `mission-02-spring-core-basic`의 `task-08-object-oriented-principles` 작업 내용을 정리한 보고서입니다.  
다형성을 활용한 결제 서비스 예제를 구현하고, 스프링 컨테이너가 여러 구현체를 관리·선택하는 방식을 확인했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles`
- 목표:
  - 결제 방식(카드, 카카오페이, 계좌이체)을 다형성 구조로 구현한다.
  - 스프링 컨테이너가 `PaymentProcessor` 구현체를 모두 빈으로 등록하는지 확인한다.
  - 런타임에 요청 결제 방식에 따라 구현체가 선택되는지 검증한다.
- 시나리오:
  - `PaymentProcessor` 인터페이스를 기준으로 구현체 3개를 분리
  - `PaymentService`가 `Map<String, PaymentProcessor>`를 주입받아 결제 타입별 구현체 선택

## 2. 구현 단계와 주요 코드

### 2.1 다형성 인터페이스와 구현체 작성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/payment/PaymentProcessor.java`
- `.../payment/CardPaymentProcessor.java`
- `.../payment/KakaoPayPaymentProcessor.java`
- `.../payment/BankTransferPaymentProcessor.java`

구현체별 결제 정책:
- `card`: 결제 수수료 3%
- `kakaopay`: 결제 수수료 2%
- `bank`: 고정 수수료 500원

핵심 포인트:
- 클라이언트(`PaymentService`)는 구체 클래스가 아닌 `PaymentProcessor` 인터페이스에만 의존합니다.
- 새 결제 수단을 추가할 때 기존 서비스 코드를 크게 변경하지 않고 구현체만 추가할 수 있습니다.

### 2.2 스프링 컨테이너의 다형성 지원 방식 확인

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/service/PaymentService.java`

`PaymentService` 생성자에서 `Map<String, PaymentProcessor>`를 주입받습니다.

이 동작으로 확인한 내용:
- 스프링 컨테이너는 `PaymentProcessor` 타입 구현체를 모두 수집해 주입합니다.
- 빈 이름(`cardPaymentProcessor`, `kakaoPayPaymentProcessor`, `bankTransferPaymentProcessor`)과 구현체 객체를 함께 관리합니다.
- 요청 `method` 값으로 적절한 구현체를 선택해 동일한 인터페이스 메서드(`pay`)를 실행합니다.

### 2.3 확인용 API 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task08_object_oriented_principles/controller/PaymentController.java`

엔드포인트:
- `GET /mission02/task08/payments/processors`: 결제 방식-빈 매핑 조회
- `POST /mission02/task08/payments`: 결제 요청 처리

요청/응답 DTO:
- `PaymentRequest`, `PaymentResponse`, `ProcessorInfoResponse`, `PaymentExecution`

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

구현체 매핑 확인:

```bash
curl http://localhost:8080/mission02/task08/payments/processors
```

예상 응답(요약):

```json
{
  "methodToBean": {
    "card": "cardPaymentProcessor",
    "kakaopay": "kakaoPayPaymentProcessor",
    "bank": "bankTransferPaymentProcessor"
  },
  "polymorphismSummary": "스프링 컨테이너가 PaymentProcessor 구현체들을 모두 빈으로 등록하고, 요청 결제 타입에 맞는 구현체를 런타임에 선택합니다."
}
```

결제 실행:

```bash
curl -X POST http://localhost:8080/mission02/task08/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"order-8001","amount":10000,"method":"kakaopay"}'
```

예상 응답(요약):

```json
{
  "orderId": "order-8001",
  "method": "kakaopay",
  "processorBean": "kakaoPayPaymentProcessor",
  "requestedAmount": 10000,
  "fee": 200,
  "approvedAmount": 9800,
  "message": "카카오페이 결제 수수료 2% 적용"
}
```

### 3.3 테스트 실행

```bash
./gradlew test --tests "*PaymentServiceTest"
```

예상 결과:
- 테스트 3건 성공
- 구현체 자동 수집/선택 로직과 예외 처리 검증 완료

## 4. 결과 확인 방법

- `GET /mission02/task08/payments/processors` 응답에서 결제 방식별 빈 이름 매핑 확인
- `POST /mission02/task08/payments` 요청 시 `method` 값을 바꿔 `processorBean`과 수수료 결과 비교
- `method=crypto` 등 미지원 값을 넣어 예외 메시지 확인

## 다형성 활용 방법 정리

- 다형성의 핵심은 “같은 인터페이스로 여러 구현체를 다룰 수 있다”는 점입니다.
- 이 예제에서 `PaymentService`는 결제 구현체의 구체 타입을 몰라도 `PaymentProcessor#pay`만 호출하면 됩니다.
- 스프링 컨테이너는 구현체를 모두 빈으로 관리하고, 주입 시점에 컬렉션(Map/List)으로 제공해 런타임 선택 전략을 쉽게 구성할 수 있습니다.
- 결과적으로 결제 수단 추가/변경이 서비스 전체에 미치는 영향을 줄이고, OCP(확장에 열리고 변경에 닫힘) 관점의 구조를 만들 수 있습니다.
