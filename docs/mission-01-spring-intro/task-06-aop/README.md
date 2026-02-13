# AOP로 서비스 로깅 적용하기

이 문서는 Spring 입문 미션(`mission-01-spring-intro`)의 여섯 번째 테스크(`task-06-aop`)에서 **@Aspect + @Around**를 사용해 서비스 메서드 실행 시간을 로깅한 과정을 정리합니다.

## 1. 구성 개요

- **패키지**: `com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop.aspect`
- **핵심 클래스**: `LoggingAspect` — 서비스 계층(pointcut) 실행 전후 시간을 측정하여 로그로 남김
- **적용 범위**: `com.goorm.springmissionsplayground.mission01_spring_intro..service..*(..)` 모든 서비스 메서드
- **의존성**: `spring-boot-starter-aop` 추가

## 2. 주요 구현

### 로깅 어스펙트
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/aspect/LoggingAspect.java`
- 포인트컷: `execution(* ..service..*(..))`
- 어드바이스: `@Around`에서 `System.nanoTime()`으로 측정 후 `log.info("[AOP][메서드] executed in xx ms")` 출력

## 3. 실행 및 확인

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### 로그 확인 방법
1. 서비스 메서드 호출 (예: 트랜잭션 목록 조회)
```bash
curl http://localhost:8080/mission01/task05/members
```
2. 콘솔 로그에서 다음 형식 확인
```
[AOP][MemberTxService.findAll(..)] executed in 3 ms
```

## 4. 학습 내용
- **AOP 개념**: 핵심 로직과 부가 기능(로깅)을 분리해 관심사 분리(SoC)를 실현한다.
- **@Around 사용 이유**: 메서드 실행 전후를 모두 다루고 반환값/예외 흐름을 제어할 수 있어 시간 측정에 적합하다.
- **포인트컷 설계**: 서비스 계층에만 적용하여 컨트롤러/리포지토리에는 영향 주지 않게 범위를 한정했다.
- **프록시 기반 동작**: Spring AOP는 런타임에 프록시를 만들어 어드바이스를 삽입한다. 기존 코드 수정 없이 로깅을 추가할 수 있다.
