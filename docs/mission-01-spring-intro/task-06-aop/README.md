# AOP로 서비스 로깅 적용하기

이 문서는 `mission-01-spring-intro`의 `task-06-aop` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-06-aop`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop`
- 코드 파일 수(테스트 포함): **2개**

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/aspect/LoggingAspect.java` | 공통 관심사(AOP) 로직 분리 |
| `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/LoggingAspectTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `LoggingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task06_aop/aspect/LoggingAspect.java`
- 역할: 공통 관심사(AOP) 로직 분리
- 상세 설명:
- 로깅/성능 측정처럼 공통 관심사를 비즈니스 코드와 분리합니다.
- 포인트컷과 어드바이스로 적용 범위를 명확히 제어합니다.

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
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

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

- **AOP(관점 지향 프로그래밍)**: 공통 관심사를 비즈니스 로직과 분리합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/aop.html
- **AspectJ 포인트컷 표현식**: 애스펙트 적용 범위를 선언적으로 지정합니다.  
  참고 문서: https://www.eclipse.org/aspectj/doc/released/progguide/semantics-pointcuts.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task06_aop*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 현재 태스크 디렉토리의 스크린샷 파일:
  - `img.png`

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
