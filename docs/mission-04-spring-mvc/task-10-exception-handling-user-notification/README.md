# 스프링 MVC: 예외 처리와 사용자 알림

이 문서는 `mission-04-spring-mvc`의 `task-10-exception-handling-user-notification` 수행 결과를 정리한 보고서입니다. 없는 자원을 조회했을 때 서비스 계층 예외를 `@ControllerAdvice`가 받아 404 알림 페이지로 전환하는 흐름을 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-10-exception-handling-user-notification`
- 목표:
  - 존재하지 않는 자원을 조회할 때 404 상태 코드와 사용자 친화적인 안내 화면을 함께 제공한다.
  - 컨트롤러, 서비스, 예외 처리기를 분리해 예외 흐름을 MVC 방식으로 정리한다.
  - Thymeleaf 템플릿으로 정상 화면과 예외 화면을 각각 렌더링한다.
- 엔드포인트: `GET /mission04/task10/error-guides`, `GET /mission04/task10/error-guides/{id}`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/domain/ErrorGuide.java` | 화면에 출력할 안내 데이터 모델 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/service/ErrorGuideService.java` | 안내 목록/상세 조회와 예외 발생 책임 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/controller/ErrorGuideController.java` | 목록/상세 화면 요청 매핑과 모델 구성 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/controller/ErrorPageControllerAdvice.java` | 컨트롤러 전역 예외를 404 화면으로 변환 |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/exception/ErrorGuideNotFoundException.java` | 없는 자원 조회 시 발생하는 커스텀 예외 |
| Template | `src/main/resources/templates/mission04/task10/error-guide-list.html` | 정상 목록 화면 템플릿 |
| Template | `src/main/resources/templates/mission04/task10/error-guide-detail.html` | 정상 상세 화면 템플릿 |
| Template | `src/main/resources/templates/mission04/task10/error-page-404.html` | 사용자 알림용 404 화면 템플릿 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/ErrorGuideControllerTest.java` | 정상/예외 흐름과 템플릿 렌더링 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `ErrorGuideService`에 인메모리 안내 데이터를 준비하고, 존재하지 않는 ID 요청 시 `ErrorGuideNotFoundException`을 던지도록 구성했습니다.
2. `ErrorGuideController`는 기본 경로 `/mission04/task10/error-guides` 아래에서 목록 화면과 상세 화면을 분리해 반환합니다.
3. `ErrorPageControllerAdvice`는 `ErrorGuideController`에서 발생한 `ErrorGuideNotFoundException`을 잡아 `404 Not Found` 상태와 함께 `error-page-404` 뷰를 반환합니다.
4. Thymeleaf 템플릿을 세 장으로 나눠 정상 목록, 정상 상세, 예외 알림 화면을 각각 별도 스타일과 메시지로 보여주도록 구성했습니다.
5. `ErrorGuideControllerTest`는 실제 `WebApplicationContext`로 `MockMvc`를 구성해 상태 코드, 뷰 이름, 모델 값, 렌더링된 문구까지 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ErrorGuide.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/domain/ErrorGuide.java`
- 역할: 화면에 출력할 안내 데이터 모델
- 상세 설명:
- 안내 페이지 하나를 표현하는 값 객체입니다.
- 상세 화면에서 사용할 제목, 설명, 권장 행동을 함께 보관합니다.
- 컨트롤러는 이 객체를 직접 모델에 담아 템플릿으로 전달합니다.

<details>
<summary><code>ErrorGuide.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain;

public class ErrorGuide {

    private final Long id;
    private final String title;
    private final String summary;
    private final String recommendedAction;

    public ErrorGuide(Long id, String title, String summary, String recommendedAction) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.recommendedAction = recommendedAction;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
}
```

</details>

### 4.2 `ErrorGuideService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/service/ErrorGuideService.java`
- 역할: 안내 목록/상세 조회와 예외 발생 책임
- 상세 설명:
- 핵심 공개 메서드: `findAll()`, `findById(Long id)`
- `LinkedHashMap`으로 학습용 데이터를 순서대로 보관하고 목록 화면에서 그대로 사용합니다.
- `findById`는 ID가 없을 때 `ErrorGuideNotFoundException`을 발생시켜 웹 계층이 404 화면으로 전환할 수 있게 만듭니다.

<details>
<summary><code>ErrorGuideService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain.ErrorGuide;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception.ErrorGuideNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ErrorGuideService {

    private final Map<Long, ErrorGuide> guides = new LinkedHashMap<>();

    public ErrorGuideService() {
        guides.put(1L, new ErrorGuide(
                1L,
                "404 에러 페이지가 왜 필요한가",
                "사용자가 없는 자원에 접근했을 때 현재 요청이 실패했음을 분명하게 알려주고, 다음 행동을 안내합니다.",
                "목록 페이지로 돌아가거나 올바른 식별자를 다시 입력하도록 유도합니다."
        ));
        guides.put(2L, new ErrorGuide(
                2L,
                "예외를 컨트롤러 밖으로 보내는 이유",
                "비즈니스 계층은 자원 조회 실패를 예외로 표현하고, 웹 계층은 이를 화면 응답으로 변환하는 역할만 담당합니다.",
                "서비스는 찾기/검증 책임에 집중하고, 사용자 알림 화면은 별도 예외 처리기로 분리합니다."
        ));
        guides.put(3L, new ErrorGuide(
                3L,
                "사용자 친화적인 알림 메시지 구성",
                "상태 코드만 노출하면 사용자는 다음 행동을 알기 어렵기 때문에 요청 경로와 복귀 경로를 함께 보여줍니다.",
                "실패 원인, 요청 경로, 돌아갈 링크를 한 화면에 제공해 재시도 흐름을 단순하게 만듭니다."
        ));
    }

    public List<ErrorGuide> findAll() {
        return new ArrayList<>(guides.values());
    }

    public ErrorGuide findById(Long id) {
        ErrorGuide guide = guides.get(id);
        if (guide == null) {
            throw new ErrorGuideNotFoundException(id);
        }
        return guide;
    }
}
```

</details>

### 4.3 `ErrorGuideController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/controller/ErrorGuideController.java`
- 역할: 목록/상세 화면 요청 매핑과 모델 구성
- 상세 설명:
- 기본 경로: `/mission04/task10/error-guides`
- HTTP 메서드/세부 경로: `GET /mission04/task10/error-guides`, `GET /mission04/task10/error-guides/{id}`
- 목록 요청에서는 전체 안내와 의도적으로 깨진 링크용 ID를 모델에 담고, 상세 요청에서는 현재 안내와 관련 안내 목록을 모델에 담아 각각 다른 뷰를 반환합니다.

<details>
<summary><code>ErrorGuideController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain.ErrorGuide;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.service.ErrorGuideService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mission04/task10/error-guides")
public class ErrorGuideController {

    private final ErrorGuideService errorGuideService;

    public ErrorGuideController(ErrorGuideService errorGuideService) {
        this.errorGuideService = errorGuideService;
    }

    @GetMapping
    public String showGuideIndex(Model model) {
        List<ErrorGuide> guides = errorGuideService.findAll();
        model.addAttribute("guides", guides);
        model.addAttribute("brokenGuideId", 999L);
        return "mission04/task10/error-guide-list";
    }

    @GetMapping("/{id}")
    public String showGuideDetail(@PathVariable Long id, Model model) {
        ErrorGuide guide = errorGuideService.findById(id);
        List<ErrorGuide> relatedGuides = errorGuideService.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(id))
                .toList();

        model.addAttribute("guide", guide);
        model.addAttribute("relatedGuides", relatedGuides);
        return "mission04/task10/error-guide-detail";
    }
}
```

</details>

### 4.4 `ErrorPageControllerAdvice.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/controller/ErrorPageControllerAdvice.java`
- 역할: 컨트롤러 전역 예외를 404 화면으로 변환
- 상세 설명:
- `@ControllerAdvice(assignableTypes = ErrorGuideController.class)`로 이번 태스크 컨트롤러 범위에만 적용했습니다.
- `@ExceptionHandler(ErrorGuideNotFoundException.class)`가 서비스 예외를 받아 모델에 상태 코드, 요청 경로, 요청 ID를 채웁니다.
- `@ResponseStatus(HttpStatus.NOT_FOUND)`로 뷰를 반환하면서도 HTTP 상태 코드는 404로 유지합니다.

<details>
<summary><code>ErrorPageControllerAdvice.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception.ErrorGuideNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = ErrorGuideController.class)
public class ErrorPageControllerAdvice {

    @ExceptionHandler(ErrorGuideNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ErrorGuideNotFoundException exception, HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorName", HttpStatus.NOT_FOUND.getReasonPhrase());
        model.addAttribute("alertTitle", "요청한 자원을 찾을 수 없습니다.");
        model.addAttribute("alertMessage", exception.getMessage());
        model.addAttribute("requestedId", exception.getGuideId());
        model.addAttribute("requestUri", request.getRequestURI());
        return "mission04/task10/error-page-404";
    }
}
```

</details>

### 4.5 `ErrorGuideNotFoundException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/exception/ErrorGuideNotFoundException.java`
- 역할: 없는 자원 조회 시 발생하는 커스텀 예외
- 상세 설명:
- 조회 실패 원인을 `Long guideId`와 함께 보관합니다.
- 예외 메시지는 사용자 알림 화면에도 그대로 재사용할 수 있게 자연어로 작성했습니다.
- 서비스는 이 예외만 던지고, 실제 404 응답 구성은 예외 처리기에 위임합니다.

<details>
<summary><code>ErrorGuideNotFoundException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.exception;

public class ErrorGuideNotFoundException extends RuntimeException {

    private final Long guideId;

    public ErrorGuideNotFoundException(Long guideId) {
        super("요청한 안내 페이지를 찾을 수 없습니다. 다시 목록에서 유효한 항목을 선택해 주세요.");
        this.guideId = guideId;
    }

    public Long getGuideId() {
        return guideId;
    }
}
```

</details>

### 4.6 `error-guide-list.html`

- 파일 경로: `src/main/resources/templates/mission04/task10/error-guide-list.html`
- 역할: 정상 목록 화면 템플릿
- 상세 설명:
- 서비스가 내려준 `guides` 목록을 `th:each`로 렌더링합니다.
- 일부러 없는 자원으로 이동하는 `404 데모 실행` 링크를 두어 예외 처리 흐름을 바로 재현할 수 있습니다.
- 안내 카드와 데모 영역을 분리해 정상 화면과 예외 화면 진입 지점을 한 페이지에서 확인할 수 있게 했습니다.

<details>
<summary><code>error-guide-list.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task10 - Error Guide Index</title>
    <style>
        :root {
            --ink: #1b2a41;
            --muted: #52606d;
            --paper: rgba(255, 255, 255, 0.88);
            --line: rgba(27, 42, 65, 0.12);
            --accent: #d16f3d;
            --accent-soft: #fff1e8;
            --bg-1: #f4efe6;
            --bg-2: #dce8f2;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Pretendard", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at top right, rgba(209, 111, 61, 0.18), transparent 25%),
                linear-gradient(160deg, var(--bg-1), var(--bg-2));
        }

        main {
            max-width: 1080px;
            margin: 0 auto;
            padding: 48px 20px 64px;
        }

        .hero,
        .guide-grid article,
        .demo-card {
            background: var(--paper);
            border: 1px solid var(--line);
            border-radius: 24px;
            box-shadow: 0 16px 40px rgba(27, 42, 65, 0.08);
            backdrop-filter: blur(10px);
        }

        .hero {
            padding: 32px;
            margin-bottom: 28px;
        }

        .eyebrow {
            display: inline-flex;
            padding: 8px 12px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.9rem;
            font-weight: 700;
        }

        h1 {
            margin: 18px 0 10px;
            font-size: clamp(2rem, 4vw, 3.4rem);
            line-height: 1.1;
        }

        p {
            margin: 0;
            color: var(--muted);
            line-height: 1.7;
        }

        .guide-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 18px;
            margin-bottom: 28px;
        }

        article {
            padding: 24px;
        }

        article strong {
            display: block;
            margin-bottom: 12px;
            font-size: 1.1rem;
        }

        .guide-id {
            display: inline-block;
            margin-bottom: 12px;
            color: var(--accent);
            font-weight: 700;
        }

        a.button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-top: 18px;
            padding: 11px 16px;
            border-radius: 999px;
            background: var(--ink);
            color: #fff;
            text-decoration: none;
            font-weight: 700;
        }

        .demo-card {
            padding: 26px;
        }

        .demo-card code {
            display: inline-block;
            margin-top: 8px;
            padding: 6px 10px;
            border-radius: 10px;
            background: #0f172a;
            color: #f8fafc;
        }
    </style>
</head>
<body>
<main>
    <section class="hero">
        <span class="eyebrow">Mission04 Task10</span>
        <h1>예외를 화면으로 바꿔서 사용자에게 설명하는 예제</h1>
        <p>
            아래 목록은 정상적으로 조회되는 안내 페이지입니다.
            존재하지 않는 ID로 접근하면 서비스 계층에서 예외가 발생하고,
            `@ControllerAdvice`가 이를 받아 사용자용 404 화면으로 전환합니다.
        </p>
    </section>

    <section class="guide-grid">
        <article th:each="guide : ${guides}">
            <span class="guide-id" th:text="|Guide #${guide.id}|">Guide #1</span>
            <strong th:text="${guide.title}">가이드 제목</strong>
            <p th:text="${guide.summary}">요약 설명</p>
            <a class="button" th:href="@{/mission04/task10/error-guides/{id}(id=${guide.id})}">상세 보기</a>
        </article>
    </section>

    <section class="demo-card">
        <strong>404 화면 확인용 링크</strong>
        <p>
            아래 경로는 일부러 존재하지 않는 ID를 가리킵니다.
            클릭하면 사용자 친화적인 404 알림 페이지가 표시됩니다.
        </p>
        <code th:text="|/mission04/task10/error-guides/${brokenGuideId}|">/mission04/task10/error-guides/999</code>
        <div>
            <a class="button" th:href="@{/mission04/task10/error-guides/{id}(id=${brokenGuideId})}">404 데모 실행</a>
        </div>
    </section>
</main>
</body>
</html>
```

</details>

### 4.7 `error-guide-detail.html`

- 파일 경로: `src/main/resources/templates/mission04/task10/error-guide-detail.html`
- 역할: 정상 상세 화면 템플릿
- 상세 설명:
- 현재 선택한 안내를 상단에 크게 표시하고, 바로 아래에 권장 행동을 분리해 배치했습니다.
- `relatedGuides`를 함께 렌더링해 상세 페이지에서 다른 정상 자원으로 이동할 수 있게 했습니다.
- 상세 페이지와 404 페이지의 대비를 위해 비교적 차분한 색상으로 구성했습니다.

<details>
<summary><code>error-guide-detail.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task10 - Error Guide Detail</title>
    <style>
        :root {
            --ink: #172033;
            --muted: #536071;
            --line: rgba(23, 32, 51, 0.1);
            --accent: #126a73;
            --accent-soft: rgba(18, 106, 115, 0.12);
            --bg-a: #f4f1ea;
            --bg-b: #d5eced;
            --card: rgba(255, 255, 255, 0.9);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Pretendard", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background:
                linear-gradient(145deg, var(--bg-a), var(--bg-b));
        }

        main {
            max-width: 960px;
            margin: 0 auto;
            padding: 48px 20px 64px;
        }

        .sheet {
            padding: 34px;
            border-radius: 28px;
            border: 1px solid var(--line);
            background: var(--card);
            box-shadow: 0 24px 56px rgba(23, 32, 51, 0.1);
        }

        .meta {
            display: inline-flex;
            padding: 8px 12px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-weight: 700;
        }

        h1 {
            margin: 18px 0 20px;
            font-size: clamp(2rem, 4vw, 3rem);
            line-height: 1.15;
        }

        p {
            margin: 0;
            color: var(--muted);
            line-height: 1.8;
        }

        .section {
            margin-top: 24px;
            padding-top: 24px;
            border-top: 1px solid var(--line);
        }

        .related-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 14px;
            margin-top: 16px;
        }

        .related-list a,
        .back-link {
            color: var(--ink);
            text-decoration: none;
            font-weight: 700;
        }

        .related-card {
            padding: 16px;
            border-radius: 18px;
            border: 1px solid var(--line);
            background: rgba(255, 255, 255, 0.74);
        }

        .back-link {
            display: inline-flex;
            margin-top: 24px;
        }
    </style>
</head>
<body>
<main>
    <section class="sheet">
        <span class="meta" th:text="|Guide #${guide.id}|">Guide #1</span>
        <h1 th:text="${guide.title}">가이드 제목</h1>
        <p th:text="${guide.summary}">가이드 설명</p>

        <div class="section">
            <strong>권장 안내 방식</strong>
            <p th:text="${guide.recommendedAction}">권장 액션</p>
        </div>

        <div class="section">
            <strong>다른 안내 페이지</strong>
            <div class="related-list">
                <div class="related-card" th:each="related : ${relatedGuides}">
                    <a th:href="@{/mission04/task10/error-guides/{id}(id=${related.id})}" th:text="${related.title}">다른 가이드</a>
                </div>
            </div>
        </div>

        <a class="back-link" th:href="@{/mission04/task10/error-guides}">목록으로 돌아가기</a>
    </section>
</main>
</body>
</html>
```

</details>

### 4.8 `error-page-404.html`

- 파일 경로: `src/main/resources/templates/mission04/task10/error-page-404.html`
- 역할: 사용자 알림용 404 화면 템플릿
- 상세 설명:
- 예외 처리기가 전달한 `errorCode`, `alertTitle`, `requestUri`, `requestedId`를 그대로 출력합니다.
- 단순히 “404”만 보여주지 않고 요청 경로와 다음 행동을 함께 안내합니다.
- 정상 목록과 정상 상세 화면으로 복귀할 수 있는 링크를 함께 제공해 사용자가 막히지 않게 했습니다.

<details>
<summary><code>error-page-404.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task10 - 404 Alert</title>
    <style>
        :root {
            --night: #1a1f36;
            --night-soft: #2b3255;
            --signal: #ff8f5a;
            --signal-soft: rgba(255, 143, 90, 0.14);
            --paper: rgba(255, 255, 255, 0.92);
            --muted: #5b6479;
            --line: rgba(26, 31, 54, 0.12);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            font-family: "Pretendard", "Noto Sans KR", sans-serif;
            background:
                radial-gradient(circle at top, rgba(255, 143, 90, 0.26), transparent 32%),
                linear-gradient(135deg, #eef2f7 0%, #d8e2f0 100%);
            color: var(--night);
        }

        .panel {
            width: min(720px, 100%);
            padding: 34px;
            border-radius: 30px;
            border: 1px solid var(--line);
            background: var(--paper);
            box-shadow: 0 24px 64px rgba(26, 31, 54, 0.14);
        }

        .badge {
            display: inline-flex;
            padding: 8px 12px;
            border-radius: 999px;
            background: var(--signal-soft);
            color: var(--signal);
            font-weight: 800;
        }

        h1 {
            margin: 16px 0 12px;
            font-size: clamp(2.2rem, 6vw, 4.4rem);
            line-height: 1;
        }

        h2 {
            margin: 0 0 16px;
            font-size: clamp(1.3rem, 3vw, 2rem);
        }

        p {
            margin: 0;
            color: var(--muted);
            line-height: 1.8;
        }

        dl {
            margin: 24px 0 0;
            padding: 18px 20px;
            border-radius: 20px;
            background: #f8fafc;
        }

        dt {
            color: var(--night-soft);
            font-weight: 700;
            margin-top: 14px;
        }

        dt:first-child {
            margin-top: 0;
        }

        dd {
            margin: 6px 0 0;
            color: var(--night);
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 28px;
        }

        .actions a {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 18px;
            border-radius: 999px;
            text-decoration: none;
            font-weight: 800;
        }

        .primary {
            background: var(--night);
            color: #fff;
        }

        .secondary {
            background: #fff;
            color: var(--night);
            border: 1px solid var(--line);
        }
    </style>
</head>
<body>
<section class="panel">
    <span class="badge" th:text="|${errorCode} ${errorName}|">404 NOT_FOUND</span>
    <h1 th:text="${errorCode}">404</h1>
    <h2 th:text="${alertTitle}">요청한 자원을 찾을 수 없습니다.</h2>
    <p th:text="${alertMessage}">에러 안내 메시지</p>

    <dl>
        <dt>요청 경로</dt>
        <dd th:text="${requestUri}">/mission04/task10/error-guides/999</dd>

        <dt>요청한 ID</dt>
        <dd th:text="${requestedId}">999</dd>

        <dt>다음 행동</dt>
        <dd>목록 화면으로 돌아가 유효한 자원을 다시 선택합니다.</dd>
    </dl>

    <div class="actions">
        <a class="primary" th:href="@{/mission04/task10/error-guides}">안내 목록으로 이동</a>
        <a class="secondary" th:href="@{/mission04/task10/error-guides/1}">정상 페이지 예시 보기</a>
    </div>
</section>
</body>
</html>
```

</details>

### 4.9 `ErrorGuideControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task10_exception_handling_user_notification/ErrorGuideControllerTest.java`
- 역할: 정상/예외 흐름과 템플릿 렌더링 검증
- 상세 설명:
- 검증 시나리오: 목록 화면 렌더링, 정상 상세 조회, 없는 ID 조회 시 404 알림 화면 반환
- 정상 흐름과 예외 흐름을 모두 보장하며, 뷰 이름/모델/문구까지 함께 검증합니다.
- `WebApplicationContext` 기반 `MockMvc`를 사용해 실제 스프링 MVC 설정과 템플릿 렌더링 흐름을 반영합니다.

<details>
<summary><code>ErrorGuideControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class ErrorGuideControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("목록 페이지는 가이드 목록과 404 데모 ID를 모델에 담아 렌더링한다")
    void showGuideIndex() throws Exception {
        mockMvc.perform(get("/mission04/task10/error-guides"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task10/error-guide-list"))
                .andExpect(model().attribute("guides", hasSize(3)))
                .andExpect(model().attribute("brokenGuideId", is(999L)))
                .andExpect(content().string(containsString("404 데모 실행")));
    }

    @Test
    @DisplayName("정상 ID로 상세 조회하면 상세 뷰를 렌더링한다")
    void showGuideDetail() throws Exception {
        mockMvc.perform(get("/mission04/task10/error-guides/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task10/error-guide-detail"))
                .andExpect(model().attributeExists("guide"))
                .andExpect(model().attribute("relatedGuides", hasSize(2)))
                .andExpect(content().string(containsString("권장 안내 방식")));
    }

    @Test
    @DisplayName("없는 ID로 조회하면 404 에러 페이지와 사용자 알림을 반환한다")
    void showNotFoundPageWhenGuideDoesNotExist() throws Exception {
        mockMvc.perform(get("/mission04/task10/error-guides/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("mission04/task10/error-page-404"))
                .andExpect(model().attribute("requestedId", is(999L)))
                .andExpect(model().attribute("requestUri", is("/mission04/task10/error-guides/999")))
                .andExpect(model().attribute("alertTitle", is("요청한 자원을 찾을 수 없습니다.")))
                .andExpect(content().string(containsString("안내 목록으로 이동")));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **`@ControllerAdvice`**
  - 핵심: 여러 컨트롤러에서 공통으로 발생하는 예외 처리나 바인딩 규칙을 한 곳에 모읍니다.
  - 왜 쓰는가: 컨트롤러마다 `try-catch`를 반복하지 않고, 예외를 사용자 화면이나 공통 응답 구조로 일관되게 변환할 수 있습니다.
  - 참고 링크: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html

- **`@ExceptionHandler`**
  - 핵심: 특정 예외 타입을 메서드 단위로 받아 원하는 뷰나 응답 본문으로 변환합니다.
  - 왜 쓰는가: 서비스나 도메인 계층에서 발생한 예외를 웹 계층에서 해석해 HTTP 응답으로 연결할 수 있습니다.
  - 참고 링크: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html

- **`@ResponseStatus`**
  - 핵심: 뷰를 반환하더라도 HTTP 상태 코드를 명시적으로 지정합니다.
  - 왜 쓰는가: 사용자 화면은 친절하게 보여주되, 클라이언트와 브라우저에는 여전히 404 오류라는 의미를 정확히 전달할 수 있습니다.
  - 참고 링크: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseStatus.html

- **Thymeleaf 템플릿**
  - 핵심: 서버가 전달한 모델 데이터를 HTML 안에서 자연스럽게 출력하는 서버 사이드 템플릿 엔진입니다.
  - 왜 쓰는가: 정상 화면과 오류 화면을 각각 분리해 두고, 상태에 따라 다른 뷰를 렌더링하기 좋습니다.
  - 참고 링크: https://www.thymeleaf.org/documentation.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 브라우저 확인

- 목록 화면: `http://localhost:8080/mission04/task10/error-guides`
- 정상 상세 화면 예시: `http://localhost:8080/mission04/task10/error-guides/1`
- 404 알림 화면 예시: `http://localhost:8080/mission04/task10/error-guides/999`

### 6.3 curl로 상태 코드 확인

```bash
curl -i http://localhost:8080/mission04/task10/error-guides/1
curl -i http://localhost:8080/mission04/task10/error-guides/999
```

예상 결과:
- `/1` 요청은 `HTTP/1.1 200`과 함께 정상 HTML을 반환합니다.
- `/999` 요청은 `HTTP/1.1 404`와 함께 사용자 알림 HTML을 반환합니다.

### 6.4 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.ErrorGuideControllerTest
```

예상 결과: 목록/정상 상세/404 알림 화면 테스트 3건이 모두 통과합니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - 목록 화면에서 안내 카드와 `404 데모 실행` 버튼이 보여야 합니다.
  - 정상 상세 화면에서는 선택한 안내 제목과 `권장 안내 방식` 문구가 보여야 합니다.
  - 없는 ID 요청 시 브라우저 상태 코드는 404이고, 화면에는 `요청한 자원을 찾을 수 없습니다.` 안내가 표시되어야 합니다.
- 스크린샷 파일명/저장 위치:
  - 목록 화면: `docs/mission-04-spring-mvc/task-10-exception-handling-user-notification/index.png`
  - 정상 상세 화면: `docs/mission-04-spring-mvc/task-10-exception-handling-user-notification/detail.png`
  - 404 알림 화면: `docs/mission-04-spring-mvc/task-10-exception-handling-user-notification/error-404.png`

## 8. 학습 내용

- 스프링 MVC에서 예외 처리는 “예외를 없애는 일”이 아니라 “예외를 사용자에게 이해 가능한 응답으로 바꾸는 일”에 가깝습니다.
- 서비스는 자원을 찾지 못했다는 사실만 예외로 표현하고, 컨트롤러 어드바이스가 그 예외를 404 화면으로 바꾸면 계층 책임이 분명해집니다.
- `@ResponseStatus`를 함께 사용하면 사용자에게는 친절한 화면을 제공하면서도 HTTP 의미는 유지할 수 있습니다.
- 정상 화면과 오류 화면을 분리해 두면 이후 400, 500 같은 다른 예외 상황도 같은 구조로 확장하기 쉬워집니다.
