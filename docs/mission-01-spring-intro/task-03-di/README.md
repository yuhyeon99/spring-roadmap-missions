# Spring DI 실습: @Autowired와 @Component 사용하기

이 문서는 `mission-01-spring-intro`의 `task-03-di` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-03-di`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task03_di`
- 코드 파일 수(테스트 포함): **9개**
- 주요 API 베이스 경로:
  - `/mission01/task03/notifications` (NotificationController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/controller/NotificationController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/EmailNotificationSender.java` | 알림 전송 채널별 구현체 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/NotificationSender.java` | 알림 전송 채널별 구현체 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/SmsNotificationSender.java` | 알림 전송 채널별 구현체 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/MessageFormatter.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/NotificationService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/NotificationServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `NotificationController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/controller/NotificationController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>NotificationController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto.NotificationRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto.NotificationResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service.NotificationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission01/task03/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse notifyAll(@RequestBody NotificationRequest request) {
        List<String> results = notificationService.notifyAllChannels(request.getMessage());
        return new NotificationResponse(results);
    }
}
```

</details>

### 4.2 `NotificationRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationRequest.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>NotificationRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto;

public class NotificationRequest {
    private String message;

    public NotificationRequest() {
    }

    public NotificationRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.3 `NotificationResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>NotificationResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto;

import java.util.List;

public class NotificationResponse {
    private final List<String> results;

    public NotificationResponse(List<String> results) {
        this.results = results;
    }

    public List<String> getResults() {
        return results;
    }
}
```

</details>

### 4.4 `EmailNotificationSender.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/EmailNotificationSender.java`
- 역할: 알림 전송 채널별 구현체
- 상세 설명:
- 전송 채널별 구현체를 분리해 DI로 교체 가능한 구조를 만듭니다.
- 인터페이스 의존으로 확장성을 확보합니다.

<details>
<summary><code>EmailNotificationSender.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public String send(String message) {
        return "[EMAIL] " + message;
    }
}
```

</details>

### 4.5 `NotificationSender.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/NotificationSender.java`
- 역할: 알림 전송 채널별 구현체
- 상세 설명:
- 전송 채널별 구현체를 분리해 DI로 교체 가능한 구조를 만듭니다.
- 인터페이스 의존으로 확장성을 확보합니다.

<details>
<summary><code>NotificationSender.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

public interface NotificationSender {
    String channel();
    String send(String message);
}
```

</details>

### 4.6 `SmsNotificationSender.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/SmsNotificationSender.java`
- 역할: 알림 전송 채널별 구현체
- 상세 설명:
- 전송 채널별 구현체를 분리해 DI로 교체 가능한 구조를 만듭니다.
- 인터페이스 의존으로 확장성을 확보합니다.

<details>
<summary><code>SmsNotificationSender.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender;

import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public String send(String message) {
        return "[SMS] " + message;
    }
}
```

</details>

### 4.7 `MessageFormatter.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/MessageFormatter.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>MessageFormatter.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String format(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return String.format("[%s] %s", timestamp, message);
    }
}
```

</details>

### 4.8 `NotificationService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/NotificationService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>NotificationService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender.NotificationSender;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final List<NotificationSender> senders;
    private final MessageFormatter formatter;

    @Autowired
    public NotificationService(List<NotificationSender> senders, MessageFormatter formatter) {
        this.senders = senders;
        this.formatter = formatter;
    }

    public List<String> notifyAllChannels(String message) {
        String formatted = formatter.format(message);
        return senders.stream()
                .map(sender -> sender.send(formatted))
                .collect(Collectors.toList());
    }
}
```

</details>

### 4.9 `NotificationServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/NotificationServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>NotificationServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void notifyAllChannels_formatsMessageAndUsesAllSenders() {
        List<String> results = notificationService.notifyAllChannels("Test message");

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).anyMatch(msg -> msg.contains("EMAIL") || msg.contains("[EMAIL]"));
        assertThat(results).anyMatch(msg -> msg.contains("SMS") || msg.contains("[SMS]"));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **의존성 주입(DI)**: 객체 생성/연결을 컨테이너가 담당해 결합도를 낮춥니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- **컴포넌트 스캔**: `@Component`, `@Service` 등 애너테이션 기반으로 빈을 자동 등록합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task03_di*"
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
