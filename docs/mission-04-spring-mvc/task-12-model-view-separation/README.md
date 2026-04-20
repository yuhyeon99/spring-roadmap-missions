# 스프링 MVC: Model과 View 분리하기

이 문서는 `mission-04-spring-mvc`의 `task-12-model-view-separation` 수행 결과를 정리한 보고서입니다. 컨트롤러가 요청을 해석한 뒤 화면에 필요한 데이터를 `Model`에 담아 넘기고, Thymeleaf 템플릿이 그 데이터를 읽어 HTML을 렌더링하는 구조를 간단한 학습 페이지로 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-12-model-view-separation`
- 목표:
  - 컨트롤러가 `Model` 객체에 데이터를 담고 뷰 이름만 반환하는 구조를 확인한다.
  - 템플릿이 `Model` 값을 읽어 화면을 구성하며, 컨트롤러가 HTML 마크업을 직접 만들지 않는다는 점을 드러낸다.
  - 코드 예제, 테스트, 실행 방법, 간략한 설명을 함께 정리해 제출 가능한 결과물로 남긴다.
- 엔드포인트: `GET /mission04/task12/model-view`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/controller/ModelViewDemoController.java` | 요청을 받고 Model 속성을 채운 뒤 뷰 이름을 반환하는 컨트롤러 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/service/ModelViewSeparationService.java` | 화면에 필요한 학습 데이터와 메시지를 생성하는 서비스 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/domain/MvcStudySession.java` | 템플릿에서 출력할 학습 세션 정보를 담는 도메인 객체 |
| Template | `src/main/resources/templates/mission04/task12/model-view-demo.html` | Model 값을 읽어 카드와 목록으로 보여주는 Thymeleaf 템플릿 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/ModelViewDemoControllerTest.java` | 기본 요청과 파라미터 변경 시 뷰 렌더링 결과를 검증하는 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `MvcStudySession` 객체에 학습 주제, 멘토, 목표, 뷰 이름을 담아 컨트롤러가 화면 정보를 하나의 객체로 다룰 수 있게 했습니다.
2. `ModelViewSeparationService`는 세션 객체, 핵심 포인트 목록, 체크리스트, 코치 메시지, 준비 날짜를 생성합니다. 화면 데이터 준비 책임을 서비스로 분리해 컨트롤러가 가벼워지도록 구성했습니다.
3. `ModelViewDemoController`는 `GET /mission04/task12/model-view` 요청을 받고, `name` 파라미터를 정리한 뒤 `pageTitle`, `learnerName`, `session`, `coachMessage`, `keyPoints`, `checklist`, `preparedDate`를 `Model`에 담습니다.
4. 컨트롤러는 뷰 이름 `mission04/task12/model-view-demo`만 반환합니다. HTML 태그 배치와 목록 반복은 템플릿이 담당하므로, 컨트롤러는 표현 레이아웃과 분리됩니다.
5. `model-view-demo.html`은 `th:text`, `th:each`를 사용해 `Model`에 담긴 객체와 목록을 화면에 출력합니다. 같은 뷰 파일을 유지한 채 `name` 파라미터 값만 바꿔도 메시지가 달라지는 점으로 Model과 View 분리를 확인할 수 있습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ModelViewDemoController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/controller/ModelViewDemoController.java`
- 역할: 요청을 받고 Model 속성을 채운 뒤 뷰 이름을 반환하는 컨트롤러
- 상세 설명:
- 기본 경로: `/mission04/task12/model-view`
- HTTP 메서드/세부 경로: `GET /mission04/task12/model-view`
- `name` 요청 파라미터가 없으면 기본값 `MVC 학습자`를 사용하고, 화면에 필요한 값을 모두 `Model`에 담은 뒤 `mission04/task12/model-view-demo` 뷰를 반환합니다.
- 상태 코드는 기본 `200 OK`이며, 응답 본문은 템플릿이 렌더링한 HTML입니다.

<details>
<summary><code>ModelViewDemoController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain.MvcStudySession;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.service.ModelViewSeparationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task12/model-view")
public class ModelViewDemoController {

    private final ModelViewSeparationService modelViewSeparationService;

    public ModelViewDemoController(ModelViewSeparationService modelViewSeparationService) {
        this.modelViewSeparationService = modelViewSeparationService;
    }

    @GetMapping
    public String showModelViewDemo(
            @RequestParam(required = false) String name,
            Model model
    ) {
        String learnerName = normalizeOrDefault(name);
        MvcStudySession session = modelViewSeparationService.createSession();

        model.addAttribute("pageTitle", "Model과 View 분리하기");
        model.addAttribute("learnerName", learnerName);
        model.addAttribute("session", session);
        model.addAttribute("coachMessage", modelViewSeparationService.coachMessage(learnerName));
        model.addAttribute("keyPoints", modelViewSeparationService.keyPoints());
        model.addAttribute("checklist", modelViewSeparationService.checklist());
        model.addAttribute("preparedDate", modelViewSeparationService.preparedDate());
        return session.getReferenceViewName();
    }

    private String normalizeOrDefault(String name) {
        if (!StringUtils.hasText(name)) {
            return "MVC 학습자";
        }
        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
```

</details>

### 4.2 `ModelViewSeparationService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/service/ModelViewSeparationService.java`
- 역할: 화면에 필요한 학습 데이터와 메시지를 생성하는 서비스
- 상세 설명:
- 핵심 공개 메서드: `createSession()`, `keyPoints()`, `checklist()`, `coachMessage(String learnerName)`, `preparedDate()`
- 트랜잭션은 사용하지 않으며, 저장소 계층 없이 뷰 데모용 데이터를 메모리에서 바로 구성합니다.
- 컨트롤러는 화면 조립만 담당하고, 실제 데이터 준비는 서비스가 맡도록 계층을 나눴습니다.

<details>
<summary><code>ModelViewSeparationService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain.MvcStudySession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ModelViewSeparationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MvcStudySession createSession() {
        return new MvcStudySession(
                "Model과 View 분리하기",
                "스프링 MVC 코치",
                "컨트롤러는 데이터를 준비하고, 뷰는 받은 데이터를 화면으로 표현하는 구조를 익힙니다.",
                "mission04/task12/model-view-demo"
        );
    }

    public List<String> keyPoints() {
        return List.of(
                "컨트롤러는 요청을 해석하고 필요한 데이터를 Model에 담습니다.",
                "뷰 템플릿은 Model 값을 읽어 HTML을 렌더링합니다.",
                "HTML 구조를 바꿔도 컨트롤러의 비즈니스 준비 로직은 그대로 둘 수 있습니다."
        );
    }

    public List<String> checklist() {
        return List.of(
                "컨트롤러가 뷰 이름을 반환하는지 확인",
                "Model 속성이 템플릿에서 올바르게 출력되는지 확인",
                "동일한 템플릿이 데이터만 바꿔 재사용될 수 있는지 확인"
        );
    }

    public String coachMessage(String learnerName) {
        return learnerName + "님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.";
    }

    public String preparedDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
```

</details>

### 4.3 `MvcStudySession.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/domain/MvcStudySession.java`
- 역할: 템플릿에서 출력할 학습 세션 정보를 담는 도메인 객체
- 상세 설명:
- 학습 주제, 멘토, 목표, 참조 뷰 이름을 보관하는 읽기 전용 객체입니다.
- 템플릿은 `${session.topic}`, `${session.mentor}`, `${session.goal}`처럼 객체 프로퍼티를 직접 읽어 화면에 출력합니다.
- 화면 관련 문자열을 한 객체에 묶어 전달하므로 `Model`의 구조도 더 명확해집니다.

<details>
<summary><code>MvcStudySession.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain;

public class MvcStudySession {

    private final String topic;
    private final String mentor;
    private final String goal;
    private final String referenceViewName;

    public MvcStudySession(String topic, String mentor, String goal, String referenceViewName) {
        this.topic = topic;
        this.mentor = mentor;
        this.goal = goal;
        this.referenceViewName = referenceViewName;
    }

    public String getTopic() {
        return topic;
    }

    public String getMentor() {
        return mentor;
    }

    public String getGoal() {
        return goal;
    }

    public String getReferenceViewName() {
        return referenceViewName;
    }
}
```

</details>

### 4.4 `model-view-demo.html`

- 파일 경로: `src/main/resources/templates/mission04/task12/model-view-demo.html`
- 역할: Model 값을 읽어 카드와 목록으로 보여주는 Thymeleaf 템플릿
- 상세 설명:
- `th:text`로 제목, 메시지, 세션 정보, 날짜를 출력합니다.
- `th:each`로 핵심 포인트 목록과 체크리스트를 반복 렌더링합니다.
- 템플릿은 표현만 담당하므로, 어떤 데이터를 보여줄지는 컨트롤러와 서비스가 결정하고 이 파일은 그 결과를 배치하는 데 집중합니다.

<details>
<summary><code>model-view-demo.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">Model과 View 분리하기</title>
    <style>
        :root {
            --ink: #172033;
            --muted: #5a6578;
            --line: #dbe4ee;
            --paper: rgba(255, 255, 255, 0.92);
            --accent: #b45309;
            --accent-soft: #fff1dd;
            --cool: #0f766e;
            --cool-soft: #def7f3;
            --bg: radial-gradient(circle at top left, #fff7ed, transparent 35%), linear-gradient(135deg, #eef6ff, #f7f8fc 48%, #edf8f4);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            color: var(--ink);
            background: var(--bg);
            font-family: "Pretendard", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif;
        }

        .page {
            max-width: 1100px;
            margin: 0 auto;
            padding: 40px 20px 72px;
        }

        .hero {
            padding: 32px;
            border: 1px solid rgba(255, 255, 255, 0.8);
            border-radius: 28px;
            background: var(--paper);
            box-shadow: 0 20px 40px rgba(23, 32, 51, 0.08);
        }

        .eyebrow {
            display: inline-flex;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 0.04em;
            text-transform: uppercase;
        }

        h1, h2, h3, p, ul {
            margin: 0;
        }

        h1 {
            margin-top: 18px;
            font-size: clamp(2.2rem, 4vw, 3.8rem);
            line-height: 1.08;
        }

        .hero-copy {
            margin-top: 18px;
            color: var(--muted);
            line-height: 1.8;
            max-width: 720px;
        }

        .hero-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-top: 28px;
        }

        .hero-card {
            padding: 18px;
            border-radius: 20px;
            background: #ffffff;
            border: 1px solid var(--line);
        }

        .hero-label {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .hero-value {
            margin-top: 8px;
            font-size: 1.08rem;
            font-weight: 700;
            line-height: 1.5;
        }

        .sections {
            display: grid;
            grid-template-columns: 1.1fr 0.9fr;
            gap: 18px;
            margin-top: 24px;
        }

        .panel {
            padding: 24px;
            border-radius: 24px;
            border: 1px solid rgba(255, 255, 255, 0.8);
            background: var(--paper);
            box-shadow: 0 16px 32px rgba(23, 32, 51, 0.06);
        }

        .panel h2 {
            font-size: 1.3rem;
        }

        .panel-intro {
            margin-top: 12px;
            color: var(--muted);
            line-height: 1.8;
        }

        .point-list,
        .check-list {
            display: grid;
            gap: 12px;
            margin-top: 18px;
            padding: 0;
            list-style: none;
        }

        .point-item,
        .check-item {
            padding: 16px 18px;
            border-radius: 18px;
            background: #ffffff;
            border: 1px solid var(--line);
            line-height: 1.7;
        }

        .point-item {
            position: relative;
            padding-left: 50px;
        }

        .point-item::before {
            content: "M";
            position: absolute;
            left: 18px;
            top: 16px;
            width: 22px;
            height: 22px;
            border-radius: 999px;
            background: var(--cool-soft);
            color: var(--cool);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 0.78rem;
            font-weight: 800;
        }

        .check-item {
            display: flex;
            gap: 12px;
            align-items: start;
        }

        .check-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 28px;
            height: 28px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.82rem;
            font-weight: 800;
        }

        .footer-note {
            margin-top: 18px;
            color: var(--muted);
            font-size: 0.92rem;
            line-height: 1.8;
        }

        @media (max-width: 860px) {
            .sections {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<main class="page">
    <section class="hero">
        <span class="eyebrow">Mission 04 Task 12</span>
        <h1 th:text="${pageTitle}">Model과 View 분리하기</h1>
        <p class="hero-copy" th:text="${coachMessage}">
            MVC 학습자님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.
        </p>

        <div class="hero-grid">
            <article class="hero-card">
                <div class="hero-label">학습 주제</div>
                <div class="hero-value" th:text="${session.topic}">Model과 View 분리하기</div>
            </article>
            <article class="hero-card">
                <div class="hero-label">멘토</div>
                <div class="hero-value" th:text="${session.mentor}">스프링 MVC 코치</div>
            </article>
            <article class="hero-card">
                <div class="hero-label">목표</div>
                <div class="hero-value" th:text="${session.goal}">
                    컨트롤러는 데이터를 준비하고, 뷰는 받은 데이터를 화면으로 표현하는 구조를 익힙니다.
                </div>
            </article>
            <article class="hero-card">
                <div class="hero-label">준비 날짜</div>
                <div class="hero-value" th:text="${preparedDate}">2026-03-20</div>
            </article>
        </div>
    </section>

    <section class="sections">
        <article class="panel">
            <h2>Model에 담아 전달한 핵심 데이터</h2>
            <p class="panel-intro">
                이 영역은 컨트롤러가 `Model`에 넣은 `session`, `coachMessage`, `preparedDate`, `keyPoints` 값을
                템플릿이 읽어 화면으로 표현한 결과입니다.
            </p>

            <ul class="point-list">
                <li class="point-item" th:each="point : ${keyPoints}" th:text="${point}">
                    컨트롤러는 요청을 해석하고 필요한 데이터를 Model에 담습니다.
                </li>
            </ul>

            <p class="footer-note">
                컨트롤러는 HTML 태그를 직접 만들지 않고, 어떤 데이터를 보여줄지만 결정합니다.
                실제 문단, 카드, 목록 배치는 템플릿 파일이 담당합니다.
            </p>
        </article>

        <article class="panel">
            <h2>View에서 확인할 체크리스트</h2>
            <p class="panel-intro">
                같은 템플릿 파일도 `Model` 값이 달라지면 다른 화면을 그릴 수 있습니다.
                아래 항목은 템플릿이 반복문으로 출력한 체크리스트입니다.
            </p>

            <ul class="check-list">
                <li class="check-item" th:each="item, stat : ${checklist}">
                    <span class="check-badge" th:text="${stat.count}">1</span>
                    <span th:text="${item}">컨트롤러가 뷰 이름을 반환하는지 확인</span>
                </li>
            </ul>
        </article>
    </section>
</main>
</body>
</html>
```

</details>

### 4.5 `ModelViewDemoControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task12_model_view_separation/ModelViewDemoControllerTest.java`
- 역할: 기본 요청과 파라미터 변경 시 뷰 렌더링 결과를 검증하는 테스트
- 상세 설명:
- 검증 시나리오 1: 기본 요청이 올바른 뷰 이름과 기본 `Model` 데이터를 담아 HTML을 렌더링하는지 확인합니다.
- 검증 시나리오 2: `name` 파라미터를 전달했을 때 같은 템플릿이 바뀐 메시지를 출력하는지 확인합니다.
- 정상 흐름만 다루지만, `Model` 값이 바뀌면 뷰 출력도 함께 달라진다는 점을 보장합니다.

<details>
<summary><code>ModelViewDemoControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation;

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
class ModelViewDemoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기본 요청은 Model 데이터를 담아 task12 뷰를 렌더링한다")
    void showModelViewDemo() throws Exception {
        mockMvc.perform(get("/mission04/task12/model-view"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task12/model-view-demo"))
                .andExpect(model().attribute("pageTitle", is("Model과 View 분리하기")))
                .andExpect(model().attribute("learnerName", is("MVC 학습자")))
                .andExpect(model().attribute("keyPoints", hasSize(3)))
                .andExpect(model().attribute("checklist", hasSize(3)))
                .andExpect(content().string(containsString("Model에 담아 전달한 핵심 데이터")))
                .andExpect(content().string(containsString("컨트롤러는 요청을 해석하고 필요한 데이터를 Model에 담습니다.")));
    }

    @Test
    @DisplayName("name 파라미터를 전달하면 Model 값이 바뀐 상태로 동일한 뷰를 렌더링한다")
    void showModelViewDemoWithCustomName() throws Exception {
        mockMvc.perform(get("/mission04/task12/model-view").param("name", "김모델"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task12/model-view-demo"))
                .andExpect(model().attribute("learnerName", is("김모델")))
                .andExpect(content().string(containsString("김모델님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.")))
                .andExpect(content().string(containsString("View에서 확인할 체크리스트")));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `Model`

- 핵심:
  - `Model`은 컨트롤러가 뷰에 전달할 데이터를 담는 저장소 역할을 합니다.
  - 뷰 템플릿은 `Model`에 담긴 이름으로 값을 찾아 출력합니다.
- 왜 쓰는가:
  - 컨트롤러가 HTML을 직접 만들지 않고도 화면에 필요한 데이터를 전달할 수 있습니다.
  - 같은 뷰 템플릿도 `Model` 데이터만 바꾸면 다른 결과를 그릴 수 있어 재사용이 쉬워집니다.
- 참고 링크:
  - Spring Framework Reference, Annotated Controllers: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

### 5.2 View 이름 반환

- 핵심:
  - 컨트롤러는 문자열 형태의 논리 뷰 이름을 반환하고, View Resolver가 이를 실제 템플릿 파일 경로로 연결합니다.
  - 이번 태스크에서는 `mission04/task12/model-view-demo`가 실제 HTML 템플릿으로 해석됩니다.
- 왜 쓰는가:
  - 컨트롤러가 템플릿 실제 경로나 렌더링 세부사항에 덜 의존하게 되어 구조가 단순해집니다.
  - 표현 기술이 바뀌어도 컨트롤러 코드를 덜 건드리고 유지할 수 있습니다.
- 참고 링크:
  - Spring Framework Reference, View Technologies: https://docs.spring.io/spring-framework/reference/web/webmvc-view.html

### 5.3 Thymeleaf `th:text`, `th:each`

- 핵심:
  - `th:text`는 단일 값을 출력하고, `th:each`는 컬렉션 데이터를 반복 출력합니다.
  - 둘을 함께 사용하면 `Model`의 문자열, 객체, 리스트를 한 화면에서 자연스럽게 표현할 수 있습니다.
- 왜 쓰는가:
  - 템플릿 안에서 데이터 출력 규칙이 명확하게 드러나고, 자바 코드와 HTML 구조 역할이 분리됩니다.
  - 화면 레이아웃은 유지하면서도 데이터 수량이나 값 변화에 유연하게 대응할 수 있습니다.
- 참고 링크:
  - Thymeleaf Official Tutorial: https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
  - Thymeleaf + Spring Tutorial: https://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 화면 접근 방법

기본 화면:

```bash
open http://localhost:8080/mission04/task12/model-view
```

이름 파라미터 전달:

```bash
open "http://localhost:8080/mission04/task12/model-view?name=%EA%B9%80%EB%AA%A8%EB%8D%B8"
```

터미널에서 HTML 일부 확인:

```bash
curl http://localhost:8080/mission04/task12/model-view
curl "http://localhost:8080/mission04/task12/model-view?name=%EA%B9%80%EB%AA%A8%EB%8D%B8"
```

예상 결과:

- 기본 요청에는 `MVC 학습자님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.` 문구가 출력됩니다.
- `name=김모델` 요청에는 같은 템플릿에서 `김모델님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다.` 문구가 출력됩니다.

### 6.3 테스트 실행

```bash
./gradlew test --tests "com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.ModelViewDemoControllerTest"
```

예상 결과:

- 기본 요청 테스트가 뷰 이름, 기본 Model 데이터, HTML 문구를 검증합니다.
- 이름 파라미터 테스트가 같은 뷰에서 출력 메시지만 달라지는지 확인합니다.

## 7. 결과 확인 방법

- 성공 기준:
  - `/mission04/task12/model-view` 접속 시 학습 주제, 멘토, 목표, 날짜, 핵심 데이터 목록, 체크리스트가 한 화면에 렌더링됩니다.
  - `name` 파라미터를 바꾸면 같은 템플릿에서 안내 메시지 텍스트만 달라집니다.
  - 컨트롤러는 뷰 이름과 `Model` 데이터만 준비하고, 템플릿이 최종 HTML 구조를 만듭니다.
- 응답 결과 예시:

```text
GET /mission04/task12/model-view
-> HTML 응답 본문에 "Model에 담아 전달한 핵심 데이터", "MVC 학습자님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다." 포함

GET /mission04/task12/model-view?name=김모델
-> HTML 응답 본문에 "김모델님, 컨트롤러는 데이터를 준비하고 View는 표현을 담당합니다." 포함
```

## 8. 학습 내용

이번 태스크에서 중요한 점은 컨트롤러와 뷰가 같은 화면을 만들더라도 맡는 역할이 다르다는 점입니다. 컨트롤러는 요청을 받고 어떤 데이터를 보여줄지 결정한 뒤 `Model`에 값을 담아 넘깁니다. 반대로 템플릿은 그 값을 어디에 어떤 모양으로 배치할지만 담당합니다. 이렇게 역할을 나누면 컨트롤러는 HTML 구조에 덜 얽매이고, 템플릿은 자바 로직을 거의 모른 채 화면 표현에 집중할 수 있습니다.

또한 같은 템플릿 파일도 `Model`에 담긴 데이터가 달라지면 다른 결과를 보여준다는 점을 확인했습니다. 이번 예제에서는 `name` 파라미터만 바꿔도 안내 문구가 달라졌습니다. 이런 구조 덕분에 스프링 MVC에서는 하나의 화면 템플릿을 유지하면서도 요청 조건에 따라 다양한 데이터를 표시할 수 있습니다.
