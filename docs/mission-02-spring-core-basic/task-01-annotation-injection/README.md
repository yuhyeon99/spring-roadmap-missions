# 스프링 핵심 원리 - 기본: 애너테이션을 사용하여 빈 주입하기

이 문서는 `mission-02-spring-core-basic`의 `task-01-annotation-injection` 작업 내용을 정리한 보고서입니다.  
스프링에서 `@Autowired`, `@Inject`, `@Qualifier`를 사용해 빈 의존성을 주입하고, 실제 요청 흐름에서 주입된 빈이 정상 동작하는지 확인했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection`
- 목표:
  - 애너테이션 기반으로 빈을 등록하고 주입한다.
  - 동일 인터페이스 구현체가 여러 개인 상황에서 주입 대상을 명확히 지정한다.
  - 주입된 빈이 API 호출 시 올바르게 동작하는지 검증한다.
- 시나리오: 이름 입력값을 정리한 뒤(`@Inject`), 선택된 인사 정책 빈으로 메시지를 생성(`@Autowired` + `@Qualifier`)하여 응답한다.

## 2. 구현 단계와 주요 코드

### 2.1 정책 빈 등록(`@Component`)

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/GreetingPolicy.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FormalGreetingPolicy.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FriendlyGreetingPolicy.java`

`GreetingPolicy` 인터페이스를 만들고, 구현체 2개를 `@Component`로 등록했습니다.  
주입 시 식별이 가능하도록 빈 이름을 `formalGreetingPolicy`, `friendlyGreetingPolicy`로 지정했습니다.

### 2.2 `@Inject`로 보조 컴포넌트 주입

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameSanitizer.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameNormalizer.java`

`NameNormalizer`는 생성자에 `@Inject`를 사용해 `NameSanitizer` 빈을 주입받습니다.  
입력 이름의 앞뒤 공백 제거, 다중 공백 정규화, 빈 문자열 기본값(`손님`) 처리를 담당합니다.

### 2.3 `@Autowired` + `@Qualifier`로 정책 선택 주입

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/AnnotationGreetingService.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/dto/GreetingResponse.java`

`AnnotationGreetingService` 생성자에 `@Autowired`를 적용하고,  
`GreetingPolicy` 타입 주입 파라미터에 `@Qualifier("formalGreetingPolicy")`를 지정해 원하는 구현체를 선택했습니다.

### 2.4 API 엔드포인트 연결

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/controller/GreetingController.java`

`GET /mission02/task01/greetings?name=...` 요청으로 서비스 결과를 반환하도록 구성했습니다.

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task01/greetings?name=%20%20%EC%8A%A4%ED%94%84%EB%A7%81%20%20%ED%95%99%EC%8A%B5%EC%9E%90%20%20"
```

예상 응답:

```json
{
  "message": "안녕하세요, 스프링 학습자님. 애너테이션 기반 빈 주입이 정상 동작했습니다.",
  "selectedPolicy": "formalGreetingPolicy",
  "injectionType": "@Autowired + @Inject"
}
```

### 3.3 테스트 실행

```bash
./gradlew test --tests "*AnnotationGreetingServiceTest"
```

예상 결과:
- 테스트 2건 성공
- 애너테이션 주입 체인(`@Autowired`, `@Inject`)이 실제 스프링 컨테이너에서 정상 동작

## 4. 결과 확인 방법

- 브라우저 또는 `curl`로 `GET /mission02/task01/greetings` 호출
- `name` 파라미터를 비우거나 공백만 전달했을 때 응답 메시지에 `손님`이 포함되는지 확인
- 테스트 리포트에서 `AnnotationGreetingServiceTest` 성공 여부 확인

## 학습 내용

- `@Component`는 클래스를 스프링 빈으로 등록하며, 동일 타입 구현체가 여러 개면 빈 이름이나 `@Qualifier`로 주입 대상을 구분해야 합니다.
- `@Autowired`는 스프링이 타입을 기준으로 의존성을 찾고 주입합니다. 생성자 주입에 사용하면 객체 생성 시점에 필요한 의존성이 강제되어 안전합니다.
- `@Inject`는 JSR-330 표준 애너테이션으로, 스프링에서도 동일하게 의존성 주입에 사용할 수 있습니다. 프레임워크에 덜 종속적인 코드 작성에 도움이 됩니다.
- 입력 정규화 책임(`NameNormalizer`)과 메시지 정책 책임(`GreetingPolicy`)을 분리하면, 기능 변경 시 영향 범위를 줄이고 테스트를 단순화할 수 있습니다.
