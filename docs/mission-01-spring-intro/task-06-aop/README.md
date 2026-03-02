# AOP로 서비스 로깅 적용하기

이 문서는 `mission-01-spring-intro`의 `task-06-aop`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-06-aop`
- 목표:
  - 서비스 메서드 실행 시간을 AOP로 공통 로깅한다.
  - 포인트컷 표현식으로 적용 범위를 `mission01_spring_intro..service`로 제한한다.
  - 테스트에서 로그 캡처로 애스펙트 적용 여부를 확인한다.
- 적용 대상: `com.goorm.springmissionsplayground.mission01_spring_intro..service..*(..)`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/aspect/LoggingAspect.java` | 공통 관심사(AOP) 분리 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/LoggingAspectTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `LoggingAspect`를 `@Aspect` + `@Component`로 등록해 스프링 AOP 프록시 체인에 포함시킵니다.
2. `@Around("execution(* ...service..*(..))")` 포인트컷으로 서비스 계층 메서드 호출 전후를 감쌉니다.
3. `ProceedingJoinPoint` 실행 시간을 측정해 메서드 시그니처와 함께 로그로 출력합니다.
4. `LoggingAspectTest`에서 ListAppender로 로그를 캡처해 실제 애스펙트 적용 여부를 확인합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `LoggingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/aspect/LoggingAspect.java`
- 역할: 공통 관심사(AOP) 분리
- 상세 설명:
- 로깅/측정 같은 공통 관심사를 비즈니스 로직과 분리합니다.
- 포인트컷으로 적용 범위를 선언하고, 어드바이스에서 전후 동작을 정의합니다.
- 애스펙트 적용 여부는 프록시 기반으로 테스트에서 검증할 수 있습니다.

<details>
<summary><code>LoggingAspect.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * mission01_spring_intro 하위의 service 패키지 메서드 실행 시간을 측정한다.
     */
    @Around("execution(* com.goorm.springmissionsplayground.mission01_spring_intro..service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.nanoTime();
            long elapsedMs = (end - start) / 1_000_000;
            log.info("[AOP][{}] executed in {} ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }
}
```

</details>

### 4.2 `LoggingAspectTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/LoggingAspectTest.java`
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `aop_logs_execution_time,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

<details>
<summary><code>LoggingAspectTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service.MemberTxService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoggingAspectTest {

    @Autowired
    MemberTxService memberTxService;

    @Test
    void aop_logs_execution_time() {
        Logger logger = (Logger) LoggerFactory.getLogger("com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop.aspect.LoggingAspect");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            memberTxService.findAll(); // 대상 메서드 실행

            assertThat(listAppender.list)
                    .anyMatch(event -> event.getFormattedMessage().contains("MemberTxService.findAll"));
        } finally {
            logger.detachAppender(listAppender);
        }
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **Spring AOP**
  - 핵심: 공통 관심사를 핵심 로직에서 분리해 재사용합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/aop.html
- **`@Around` Advice + Pointcut**
  - 핵심: 메서드 실행 전/후를 감싸 실행 시간을 측정할 수 있습니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 애스펙트 로그 확인

서비스 메서드를 호출하는 API 예시:

```bash
curl http://localhost:8080/mission01/task05/members
```

예상 로그: `[AOP][...] executed in ... ms`

### 6.3 테스트

```bash
./gradlew test --tests "*task06_aop*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- AOP는 로깅처럼 반복되는 공통 코드를 비즈니스 로직에서 분리하는 데 효과적입니다.
- 포인트컷 범위를 좁게 설계하면 의도하지 않은 메서드에 부작용 없이 적용할 수 있습니다.
