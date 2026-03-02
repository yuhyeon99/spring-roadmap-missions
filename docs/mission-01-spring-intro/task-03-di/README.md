# Spring DI 실습: @Autowired와 @Component 사용하기

이 문서는 `mission-01-spring-intro`의 `task-03-di`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-03-di`
- 목표:
  - `@Component` 기반 빈 등록과 생성자 주입을 사용해 DI 흐름을 구성한다.
  - `NotificationSender` 구현체를 컬렉션 주입(`List<NotificationSender>`)으로 한 번에 받아 다중 채널 전송을 수행한다.
  - 메시지 포맷터와 전송 책임을 분리해 변경에 유연한 구조를 만든다.
- 엔드포인트: `POST /mission01/task03/notifications`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/controller/NotificationController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationRequest.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/dto/NotificationResponse.java` | 요청/응답 데이터 구조 |
| Sender | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/EmailNotificationSender.java` | 알림 채널별 전송 구현 |
| Sender | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/NotificationSender.java` | 알림 채널별 전송 구현 |
| Sender | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/sender/SmsNotificationSender.java` | 알림 채널별 전송 구현 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/MessageFormatter.java` | 비즈니스 로직과 흐름 제어 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/service/NotificationService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/NotificationServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `NotificationSender` 인터페이스를 기준으로 Email/SMS 구현체를 각각 빈으로 등록합니다.
2. `NotificationService` 생성자에서 `List<NotificationSender>`를 주입받아 채널 개수와 무관하게 순회 전송합니다.
3. `MessageFormatter`를 별도 빈으로 분리해 메시지 전처리 책임을 분리합니다.
4. `NotificationController`는 요청 메시지를 받아 서비스 결과(채널별 전송 결과 목록)를 응답 DTO로 반환합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `NotificationController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task03_di/controller/NotificationController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission01/task03/notifications`
- 매핑 메서드: Post;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 알림 채널별 전송 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

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
- 역할: 알림 채널별 전송 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

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
- 역할: 알림 채널별 전송 구현
- 상세 설명:
- 태스크 동작에 필요한 구성요소로, 상위 계층과의 연결 지점을 담당합니다.
- 단일 책임 원칙을 유지해 변경 시 영향 범위를 줄입니다.
- 테스트 코드와 함께 읽으면 설계 의도를 더 명확히 파악할 수 있습니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class MessageFormatter {,    public String format(String message) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class NotificationService {,    public NotificationService(List<NotificationSender> senders, MessageFormatter formatter) {,    public List<String> notifyAllChannels(String message) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `notifyAllChannels_formatsMessageAndUsesAllSenders,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

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

- **의존성 주입(DI)**
  - 핵심: 객체 생성/연결을 스프링 컨테이너가 담당합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
- **컬렉션 주입(List Bean Injection)**
  - 핵심: 동일 타입 구현체 여러 개를 한 번에 주입받아 순회 처리할 수 있습니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl -X POST http://localhost:8080/mission01/task03/notifications \
  -H "Content-Type: application/json" \
  -d '{"message":"DI 실습 메시지"}'
```

예상 결과: 이메일/SMS 채널 결과가 배열 형태로 반환

### 6.3 테스트

```bash
./gradlew test --tests "*task03_di*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- DI를 사용하면 객체 생성/연결 책임이 컨테이너로 이동해 클래스 결합도가 낮아집니다.
- 컬렉션 주입은 다중 구현체를 확장 가능한 방식으로 처리할 때 유용합니다.
