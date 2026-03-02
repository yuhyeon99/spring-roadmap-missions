# 스프링 핵심 원리 - 기본: 스프링 MVC를 이용하여 간단한 웹 페이지 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-05-spring-mvc-web-page` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-05-spring-mvc-web-page`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page`
- 코드 파일 수(테스트 포함): **5개**
- 주요 API 베이스 경로:
  - `/mission02/task05/mvc` (SimpleMvcPageController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/controller/SimpleMvcPageController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/dto/LearningRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/service/MvcPageContentService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/resources/templates/mission02/task05/home.html` | Thymeleaf 기반 화면 렌더링 템플릿 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/SimpleMvcPageControllerTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `SimpleMvcPageController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/controller/SimpleMvcPageController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>SimpleMvcPageController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.dto.LearningRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service.MvcPageContentService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission02/task05/mvc")
public class SimpleMvcPageController {

    private static final String DEFAULT_NAME = "학습자";
    private static final String DEFAULT_TOPIC = "Spring MVC";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MvcPageContentService mvcPageContentService;

    public SimpleMvcPageController(MvcPageContentService mvcPageContentService) {
        this.mvcPageContentService = mvcPageContentService;
    }

    @GetMapping
    public String showPage(@RequestParam(required = false) String name, Model model) {
        String displayName = normalizeOrDefault(name, DEFAULT_NAME);
        renderModel(model, displayName, DEFAULT_TOPIC, false);
        return "mission02/task05/home";
    }

    @PostMapping("/preview")
    public String previewPage(@ModelAttribute LearningRequest learningRequest, Model model) {
        String displayName = normalizeOrDefault(learningRequest.getName(), DEFAULT_NAME);
        String topic = normalizeOrDefault(learningRequest.getTopic(), DEFAULT_TOPIC);
        renderModel(model, displayName, topic, true);
        return "mission02/task05/home";
    }

    private void renderModel(Model model, String displayName, String topic, boolean submitted) {
        model.addAttribute("displayName", displayName);
        model.addAttribute("topic", topic);
        model.addAttribute("submitted", submitted);
        model.addAttribute("serverTime", LocalDateTime.now().format(TIME_FORMATTER));
        model.addAttribute("welcomeMessage", mvcPageContentService.welcomeMessage(displayName, topic));
        model.addAttribute("learningChecklist", mvcPageContentService.learningChecklist(topic));
        model.addAttribute("learningRequest", new LearningRequest());
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return value.trim().replaceAll("\\s{2,}", " ");
    }
}
```

</details>

### 4.2 `LearningRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/dto/LearningRequest.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>LearningRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.dto;

public class LearningRequest {

    private String name;
    private String topic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
```

</details>

### 4.3 `MvcPageContentService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/service/MvcPageContentService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>MvcPageContentService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MvcPageContentService {

    public String welcomeMessage(String name, String topic) {
        return name + "님, " + topic + " 학습을 위한 MVC 데모 페이지입니다.";
    }

    public List<String> learningChecklist(String topic) {
        return List.of(
                "Controller: 요청 URL을 받아 Model과 View를 연결",
                "Model: 뷰 렌더링에 필요한 데이터 전달",
                "View(Thymeleaf): 서버 데이터를 HTML로 출력",
                "현재 학습 주제: " + topic
        );
    }
}
```

</details>

### 4.4 `home.html`

- 파일 경로: `src/main/resources/templates/mission02/task05/home.html`
- 역할: Thymeleaf 기반 화면 렌더링 템플릿
- 상세 설명:
- 서버에서 모델 데이터를 주입해 사용자 화면을 렌더링합니다.
- 요청 흐름과 화면 표현 계층을 분리해 MVC 구조를 명확히 유지합니다.

<details>
<summary><code>home.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission02 Task05 - Spring MVC Page</title>
    <style>
        :root {
            --bg-top: #f5efe6;
            --bg-bottom: #d6e9f8;
            --card: #ffffff;
            --title: #1f2a44;
            --body: #374151;
            --accent: #cc5a2b;
            --accent-soft: #ffe4d6;
            --border: #e5e7eb;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Gowun Dodum", "Noto Sans KR", sans-serif;
            color: var(--body);
            background: linear-gradient(155deg, var(--bg-top), var(--bg-bottom));
            display: grid;
            place-items: center;
            padding: 24px;
        }

        .layout {
            width: min(880px, 100%);
            display: grid;
            gap: 20px;
            animation: float-in 0.5s ease-out;
        }

        .hero {
            background: var(--card);
            border-radius: 20px;
            padding: 28px;
            border: 1px solid var(--border);
            box-shadow: 0 14px 30px rgba(17, 24, 39, 0.1);
        }

        h1 {
            margin: 0 0 10px;
            color: var(--title);
            font-size: clamp(1.5rem, 2.8vw, 2.1rem);
        }

        .badge {
            display: inline-block;
            font-size: 0.85rem;
            background: var(--accent-soft);
            color: var(--accent);
            padding: 6px 12px;
            border-radius: 999px;
            margin-bottom: 12px;
        }

        .welcome {
            margin: 8px 0 0;
            font-size: 1.05rem;
            line-height: 1.6;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .panel {
            background: var(--card);
            border-radius: 18px;
            border: 1px solid var(--border);
            padding: 22px;
        }

        .panel h2 {
            margin: 0 0 14px;
            color: var(--title);
            font-size: 1.1rem;
        }

        ul {
            margin: 0;
            padding-left: 18px;
            line-height: 1.7;
        }

        form {
            display: grid;
            gap: 12px;
        }

        label {
            font-weight: 700;
            font-size: 0.95rem;
        }

        input {
            width: 100%;
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 10px 12px;
            font-size: 1rem;
        }

        button {
            border: 0;
            border-radius: 12px;
            padding: 11px 14px;
            font-size: 0.95rem;
            font-weight: 700;
            color: #fff;
            background: linear-gradient(120deg, #ca6f1f, #dc2626);
            cursor: pointer;
        }

        .meta {
            margin-top: 10px;
            font-size: 0.9rem;
            color: #6b7280;
        }

        .notice {
            margin-top: 8px;
            font-size: 0.9rem;
            color: #0f766e;
        }

        @media (max-width: 760px) {
            .grid {
                grid-template-columns: 1fr;
            }
            .hero,
            .panel {
                padding: 18px;
            }
        }

        @keyframes float-in {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>
<body>
<main class="layout">
    <section class="hero">
        <span class="badge">Mission02 Task05</span>
        <h1>Spring MVC 간단 웹 페이지</h1>
        <p class="welcome" th:text="${welcomeMessage}">
            학습자님, Spring MVC 학습을 위한 MVC 데모 페이지입니다.
        </p>
        <p class="meta">
            현재 선택한 주제: <strong th:text="${topic}">Spring MVC</strong> |
            렌더링 시각: <strong th:text="${serverTime}">2026-02-26 20:10:00</strong>
        </p>
        <p class="notice" th:if="${submitted}">
            폼 입력이 컨트롤러로 전달되어 Model 값이 갱신되었습니다.
        </p>
    </section>

    <section class="grid">
        <article class="panel">
            <h2>학습 체크리스트</h2>
            <ul>
                <li th:each="item : ${learningChecklist}" th:text="${item}">Controller와 View 연결 확인</li>
            </ul>
        </article>

        <article class="panel">
            <h2>Model 데이터 바꿔보기</h2>
            <form th:action="@{/mission02/task05/mvc/preview}" method="post" th:object="${learningRequest}">
                <div>
                    <label for="name">이름</label>
                    <input id="name" type="text" th:field="*{name}" placeholder="예: 김스프링">
                </div>
                <div>
                    <label for="topic">주제</label>
                    <input id="topic" type="text" th:field="*{topic}" placeholder="예: Model/View 분리">
                </div>
                <button type="submit">Model 반영해서 다시 렌더링</button>
            </form>
            <p class="meta">현재 화면 기준 이름: <strong th:text="${displayName}">학습자</strong></p>
        </article>
    </section>
</main>
</body>
</html>
```

</details>

### 4.5 `SimpleMvcPageControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/SimpleMvcPageControllerTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>SimpleMvcPageControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.controller.SimpleMvcPageController;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service.MvcPageContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SimpleMvcPageControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SimpleMvcPageController controller = new SimpleMvcPageController(new MvcPageContentService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void showPage_rendersViewAndModel() throws Exception {
        mockMvc.perform(get("/mission02/task05/mvc").param("name", "  김스프링  "))
                .andExpect(status().isOk())
                .andExpect(view().name("mission02/task05/home"))
                .andExpect(model().attribute("displayName", "김스프링"))
                .andExpect(model().attribute("topic", "Spring MVC"))
                .andExpect(model().attribute("submitted", false))
                .andExpect(model().attributeExists("welcomeMessage", "serverTime", "learningRequest"))
                .andExpect(model().attribute("learningChecklist", hasSize(4)));
    }

    @Test
    void previewPage_bindsFormValuesAndRendersView() throws Exception {
        mockMvc.perform(post("/mission02/task05/mvc/preview")
                        .param("name", "   ")
                        .param("topic", "Model 과 View 분리"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission02/task05/home"))
                .andExpect(model().attribute("displayName", "학습자"))
                .andExpect(model().attribute("topic", "Model 과 View 분리"))
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attributeExists("welcomeMessage", "serverTime", "learningChecklist"));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **Spring MVC + Thymeleaf**: 컨트롤러가 모델 데이터를 전달하고 템플릿이 렌더링합니다.  
  공식 문서: https://docs.spring.io/spring-boot/reference/web/servlet.html
- **모델 바인딩**: 폼 입력을 DTO로 매핑해 서버 렌더링 흐름을 구성합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/modelattrib-method-args.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task05_spring_mvc_web_page*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 필요 시 실행 결과를 캡처해 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
