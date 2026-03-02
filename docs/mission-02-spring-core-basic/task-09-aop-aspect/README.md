# 스프링 핵심 원리 - 기본: AOP를 사용하여 애스펙트 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-09-aop-aspect` 작업 내용을 정리한 보고서입니다.  
Spring AOP를 사용해 애스펙트를 정의하고, 애너테이션 기반으로 타깃 메서드에 적용해 동작을 검증했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect`
- 목표:
  - `@Aspect`로 실행 시간 로깅 애스펙트를 구현한다.
  - 커스텀 애너테이션(`@TrackExecution`)을 타깃 메서드에 적용한다.
  - 테스트로 AOP 프록시 생성과 애스펙트 실행 여부를 확인한다.
- 엔드포인트:
  - `GET /mission02/task09/aspect/demo?topic=...`

## 2. 구현 단계와 주요 코드

### 2.1 타깃 지정용 커스텀 애너테이션 정의

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/annotation/TrackExecution.java`

`@TrackExecution`은 메서드에 부착할 수 있도록 `@Target(ElementType.METHOD)`로 제한하고,  
런타임에 리플렉션으로 읽을 수 있도록 `@Retention(RetentionPolicy.RUNTIME)`으로 설정했습니다.

### 2.2 애스펙트 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/aspect/ExecutionLoggingAspect.java`

`@Around("@annotation(trackExecution)")` 포인트컷으로 `@TrackExecution`이 선언된 메서드만 감쌉니다.

핵심 동작:
- 메서드 시작 시점과 종료 시점의 시간을 측정
- `joinPoint.proceed()`로 원본 로직 실행
- 최종적으로 `[TASK09-AOP][메서드명] executed in n ms` 형식의 로그 출력

### 2.3 타깃 서비스와 API 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/service/AspectDemoService.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/controller/AspectDemoController.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/dto/AspectDemoResponse.java`

구성:
- `buildSummary(String topic)` 메서드에 `@TrackExecution`을 적용해 애스펙트 대상 지정
- `ping()` 메서드는 애너테이션 없이 두어 비교 검증에 사용
- 컨트롤러는 `topic` 파라미터를 받아 서비스 호출 결과를 JSON으로 반환

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task09/aspect/demo?topic=AOP"
```

예상 응답(요약):

```json
{
  "topic": "AOP",
  "result": "aop 학습 요청에 대해 애스펙트 로깅이 적용되었습니다."
}
```

로그 예상:
- `ExecutionLoggingAspect`에서 `AspectDemoService.buildSummary(..)` 실행 시간 로그 출력

### 3.3 테스트 실행

```bash
./gradlew test --tests "*mission02_spring_core_basic.task09_aop_aspect*"
```

예상 결과:
- 테스트 2건 성공
- `AopUtils.isAopProxy(...) == true` 검증
- `buildSummary`는 로그가 남고, `ping`은 로그가 남지 않음 검증

## 4. 결과 확인 방법

- `GET /mission02/task09/aspect/demo` 호출 후 정상 JSON 응답 확인
- 애플리케이션 콘솔에서 `[TASK09-AOP]` 로그 확인
- 테스트 실행 결과에서 task09 AOP 테스트 통과 여부 확인
- 필요하면 IDE 실행 콘솔을 캡처해 `docs/mission-02-spring-core-basic/task-09-aop-aspect/`에 이미지로 보관

## 5. 학습 내용

- AOP는 핵심 비즈니스 로직과 공통 관심사(로깅, 트랜잭션, 보안)를 분리하기 위한 방식입니다.
- `@Aspect` + 포인트컷으로 “어디에 적용할지”를 선언하고, `@Around`로 “실행 전후에 무엇을 할지”를 정의합니다.
- 스프링은 대상 빈을 프록시로 감싸고, 메서드 호출 시 프록시가 먼저 동작하여 애스펙트 로직을 실행합니다.
- 애너테이션 기반 포인트컷은 적용 범위를 명확히 제어할 수 있어, 원하는 메서드에만 공통 로직을 안전하게 붙일 수 있습니다.
