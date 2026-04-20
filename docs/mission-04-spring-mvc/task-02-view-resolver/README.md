# 스프링 MVC: View Resolver 설정과 활용

이 문서는 `mission-04-spring-mvc`의 `task-02-view-resolver` 수행 결과를 정리한 보고서입니다. 컨트롤러가 반환한 논리 뷰 이름을 Thymeleaf View Resolver가 실제 템플릿으로 연결하고, `Model`에 담은 데이터를 화면에 출력하는 흐름을 실습했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-02-view-resolver`
- 목표:
  - Thymeleaf 기반 View Resolver 설정을 명시해 논리 뷰 이름과 실제 템플릿 경로의 연결 방식을 확인한다.
  - 컨트롤러에서 `Model` 데이터를 담고 템플릿에서 값을 출력하는 서버 렌더링 흐름을 구현한다.
  - 설정 파일, 컨트롤러, 서비스, 템플릿, 테스트를 함께 남겨 재현 가능한 결과물로 정리한다.
- 엔드포인트: `GET /mission04/task02/view-resolver`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Config | `src/main/resources/application.properties` | Thymeleaf View Resolver prefix/suffix/cache 설정 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/controller/ViewResolverController.java` | 요청 매핑과 모델 데이터 구성 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/service/ViewResolverDemoService.java` | 뷰 설명용 메시지와 예제 데이터 생성 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/domain/ViewResolverStudyItem.java` | View Resolver 흐름 한 단계를 표현하는 데이터 모델 |
| Template | `src/main/resources/templates/mission04/task02/view-resolver-demo.html` | Thymeleaf 템플릿 렌더링 화면 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/ViewResolverControllerTest.java` | 뷰 이름, 모델 데이터, 렌더링 결과 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `application.properties`에 `spring.thymeleaf.prefix`, `spring.thymeleaf.suffix`, `spring.thymeleaf.mode`, `spring.thymeleaf.encoding`, `spring.thymeleaf.cache`를 명시해 View Resolver가 어떤 규칙으로 템플릿을 찾는지 설정 파일 수준에서 드러냈습니다.
2. `ViewResolverController`는 `GET /mission04/task02/view-resolver` 요청을 받아 논리 뷰 이름 `mission04/task02/view-resolver-demo`를 반환합니다. 이때 `displayName`, `logicalViewName`, `resolvedTemplatePath`, `resolverFlow` 같은 모델 데이터를 함께 담습니다.
3. `ViewResolverDemoService`는 화면 설명용 문자열과 View Resolver 동작 순서 목록을 만들어 컨트롤러가 화면 조립 책임만 갖도록 분리했습니다.
4. `view-resolver-demo.html`은 `th:text`, `th:each`로 단일 값과 목록 데이터를 출력합니다. 화면에는 논리 뷰 이름과 실제 템플릿 경로를 동시에 노출해 View Resolver의 역할을 눈으로 확인할 수 있게 했습니다.
5. `ViewResolverControllerTest`는 MockMvc로 상태 코드, 뷰 이름, 모델 속성, 렌더링된 HTML 문구를 함께 검증해 설정과 화면이 실제로 연결되는지 확인합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: Thymeleaf View Resolver prefix/suffix/cache 설정
- 상세 설명:
- `spring.thymeleaf.prefix=classpath:/templates/`와 `spring.thymeleaf.suffix=.html` 조합으로 논리 뷰 이름이 실제 HTML 템플릿 경로로 해석됩니다.
- `spring.thymeleaf.mode=HTML`, `spring.thymeleaf.encoding=UTF-8`로 템플릿 처리 모드와 문자 인코딩을 명시했습니다.
- `spring.thymeleaf.cache=false`로 개발 중 템플릿 수정 사항이 바로 반영되도록 설정했습니다.

<details>
<summary><code>application.properties</code> 전체 코드</summary>

```properties
spring.application.name=core

# Mission04 Task02: Thymeleaf View Resolver 설정
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

# H2 in-memory DB 설정 (테스트/학습용)
spring.datasource.url=jdbc:h2:mem:mission01;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 콘솔 (개발 편의를 위해 활성화)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

</details>

### 4.2 `ViewResolverController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/controller/ViewResolverController.java`
- 역할: 요청 매핑과 모델 데이터 구성
- 상세 설명:
- 기본 경로: `/mission04/task02/view-resolver`
- HTTP 메서드/세부 경로: `GET /mission04/task02/view-resolver`
- `name` 요청 파라미터가 비어 있으면 기본값 `MVC 학습자`를 사용하고, 최종적으로 논리 뷰 이름 `mission04/task02/view-resolver-demo`를 반환합니다.
- 상태 코드는 기본 `200 OK`이며, 응답 본문은 Thymeleaf가 렌더링한 HTML입니다. 컨트롤러는 View Resolver 학습에 필요한 핵심 모델 속성을 모두 채웁니다.

<details>
<summary><code>ViewResolverController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.service.ViewResolverDemoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task02/view-resolver")
public class ViewResolverController {

    private static final String DEFAULT_NAME = "MVC 학습자";
    private static final String LOGICAL_VIEW_NAME = "mission04/task02/view-resolver-demo";
    private static final String RESOLVED_TEMPLATE_PATH = "classpath:/templates/mission04/task02/view-resolver-demo.html";

    private final ViewResolverDemoService viewResolverDemoService;

    public ViewResolverController(ViewResolverDemoService viewResolverDemoService) {
        this.viewResolverDemoService = viewResolverDemoService;
    }

    @GetMapping
    public String showViewResolverPage(@RequestParam(required = false) String name, Model model) {
        String displayName = normalizeOrDefault(name);

        model.addAttribute("pageTitle", "View Resolver 설정과 활용");
        model.addAttribute("displayName", displayName);
        model.addAttribute("templateEngine", "Thymeleaf");
        model.addAttribute("logicalViewName", LOGICAL_VIEW_NAME);
        model.addAttribute("resolvedTemplatePath", RESOLVED_TEMPLATE_PATH);
        model.addAttribute("welcomeMessage", viewResolverDemoService.welcomeMessage(displayName));
        model.addAttribute("resolverFlow", viewResolverDemoService.resolverFlow());
        model.addAttribute("modelExamples", viewResolverDemoService.modelExamples(displayName));
        model.addAttribute("renderedAt", viewResolverDemoService.renderedAt());
        return LOGICAL_VIEW_NAME;
    }

    private String normalizeOrDefault(String name) {
        if (!StringUtils.hasText(name)) {
            return DEFAULT_NAME;
        }
        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
```

</details>

### 4.3 `ViewResolverDemoService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/service/ViewResolverDemoService.java`
- 역할: 뷰 설명용 메시지와 예제 데이터 생성
- 상세 설명:
- 핵심 공개 메서드: `welcomeMessage(String displayName)`, `resolverFlow()`, `modelExamples(String displayName)`, `renderedAt()`
- 트랜잭션은 사용하지 않으며, 저장소 계층 없이 화면 실습용 데이터를 메모리에서 바로 생성합니다.
- 컨트롤러는 서비스가 만들어 준 문자열과 목록을 `Model`에 담기만 하므로, 화면 조립 책임과 데이터 준비 책임이 분리됩니다.

<details>
<summary><code>ViewResolverDemoService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.domain.ViewResolverStudyItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ViewResolverDemoService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String welcomeMessage(String displayName) {
        return displayName + "님, 컨트롤러가 반환한 논리 뷰 이름을 View Resolver가 실제 템플릿으로 연결합니다.";
    }

    public List<ViewResolverStudyItem> resolverFlow() {
        return List.of(
                new ViewResolverStudyItem(
                        "1. Controller 반환",
                        "컨트롤러는 `mission04/task02/view-resolver-demo` 같은 논리 뷰 이름만 반환합니다."
                ),
                new ViewResolverStudyItem(
                        "2. View Resolver 선택",
                        "Thymeleaf View Resolver가 prefix와 suffix 설정을 조합해 실제 템플릿 경로를 찾습니다."
                ),
                new ViewResolverStudyItem(
                        "3. 템플릿 렌더링",
                        "Model에 담긴 데이터를 HTML에 바인딩해 브라우저로 최종 응답을 보냅니다."
                )
        );
    }

    public List<String> modelExamples(String displayName) {
        return List.of(
                "displayName = " + displayName,
                "templateEngine = Thymeleaf",
                "resolvedTemplatePath = classpath:/templates/mission04/task02/view-resolver-demo.html"
        );
    }

    public String renderedAt() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
```

</details>

### 4.4 `ViewResolverStudyItem.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/domain/ViewResolverStudyItem.java`
- 역할: View Resolver 흐름 한 단계를 표현하는 데이터 모델
- 상세 설명:
- 화면에 보여줄 단계명(`stage`)과 설명(`description`)을 함께 보관하는 단순 읽기 전용 객체입니다.
- 템플릿의 `th:each`가 객체 리스트를 순회하면서 단계별 설명을 출력할 때 사용합니다.
- 값 객체를 별도 파일로 분리해 문자열 목록보다 화면 의미가 더 명확하게 드러나도록 했습니다.

<details>
<summary><code>ViewResolverStudyItem.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.domain;

public class ViewResolverStudyItem {

    private final String stage;
    private final String description;

    public ViewResolverStudyItem(String stage, String description) {
        this.stage = stage;
        this.description = description;
    }

    public String getStage() {
        return stage;
    }

    public String getDescription() {
        return description;
    }
}
```

</details>

### 4.5 `view-resolver-demo.html`

- 파일 경로: `src/main/resources/templates/mission04/task02/view-resolver-demo.html`
- 역할: Thymeleaf 템플릿 렌더링 화면
- 상세 설명:
- `th:text`로 단일 모델 값(`pageTitle`, `welcomeMessage`, `displayName`, `logicalViewName`, `resolvedTemplatePath`)을 출력합니다.
- `th:each`로 `resolverFlow`, `modelExamples` 목록을 순회해 여러 항목을 화면 카드로 렌더링합니다.
- 한 화면 안에서 논리 뷰 이름과 실제 템플릿 경로를 동시에 보여줘 View Resolver가 어떤 연결을 수행하는지 바로 확인할 수 있습니다.

<details>
<summary><code>view-resolver-demo.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task02 - View Resolver</title>
    <style>
        :root {
            --bg: linear-gradient(135deg, #f3efe4, #dce7f2);
            --card: rgba(255, 255, 255, 0.92);
            --ink: #203040;
            --subtle: #5f6f7f;
            --accent: #b45309;
            --accent-soft: #fde6c8;
            --line: #d7dde5;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            padding: 28px;
            font-family: "SUIT", "Pretendard", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background: var(--bg);
        }

        .page {
            width: min(960px, 100%);
            margin: 0 auto;
            display: grid;
            gap: 20px;
        }

        .hero,
        .panel {
            background: var(--card);
            border: 1px solid var(--line);
            border-radius: 24px;
            box-shadow: 0 18px 40px rgba(32, 48, 64, 0.08);
        }

        .hero {
            padding: 32px;
        }

        .badge {
            display: inline-block;
            padding: 7px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.9rem;
            font-weight: 700;
        }

        h1,
        h2 {
            margin: 0;
        }

        h1 {
            margin-top: 14px;
            font-size: clamp(1.9rem, 4vw, 2.6rem);
        }

        .summary {
            margin: 14px 0 0;
            color: var(--subtle);
            line-height: 1.7;
            font-size: 1rem;
        }

        .meta {
            margin-top: 18px;
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            color: var(--subtle);
            font-size: 0.95rem;
        }

        .grid {
            display: grid;
            grid-template-columns: 1.1fr 0.9fr;
            gap: 20px;
        }

        .panel {
            padding: 24px;
        }

        .panel-title {
            margin-bottom: 16px;
            font-size: 1.15rem;
        }

        .flow-list,
        .example-list {
            display: grid;
            gap: 12px;
        }

        .flow-item,
        .example-item {
            border: 1px solid var(--line);
            border-radius: 18px;
            padding: 16px;
            background: #ffffff;
        }

        .flow-item strong,
        .example-item strong {
            display: block;
            margin-bottom: 6px;
        }

        .path-box {
            margin-top: 14px;
            padding: 16px;
            border-radius: 18px;
            background: #1f2937;
            color: #f9fafb;
            font-family: "JetBrains Mono", "D2Coding", monospace;
            overflow-x: auto;
        }

        .caption {
            margin: 0 0 8px;
            color: var(--subtle);
            font-size: 0.92rem;
        }

        @media (max-width: 780px) {
            body {
                padding: 18px;
            }

            .hero,
            .panel {
                padding: 20px;
            }

            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<main class="page">
    <section class="hero">
        <span class="badge">Mission04 Task02</span>
        <h1 th:text="${pageTitle}">View Resolver 설정과 활용</h1>
        <p class="summary" th:text="${welcomeMessage}">
            MVC 학습자님, 컨트롤러가 반환한 논리 뷰 이름을 View Resolver가 실제 템플릿으로 연결합니다.
        </p>
        <div class="meta">
            <span>템플릿 엔진: <strong th:text="${templateEngine}">Thymeleaf</strong></span>
            <span>렌더링 시각: <strong th:text="${renderedAt}">2026-03-18 20:10:00</strong></span>
        </div>
        <p class="caption">View Resolver가 선택한 뷰</p>
        <div class="path-box" th:text="${logicalViewName}">mission04/task02/view-resolver-demo</div>
    </section>

    <section class="grid">
        <article class="panel">
            <h2 class="panel-title">View Resolver 동작 흐름</h2>
            <div class="flow-list">
                <div class="flow-item" th:each="item : ${resolverFlow}">
                    <strong th:text="${item.stage}">1. Controller 반환</strong>
                    <span th:text="${item.description}">논리 뷰 이름 반환</span>
                </div>
            </div>
        </article>

        <article class="panel">
            <h2 class="panel-title">Model 데이터 출력 예제</h2>
            <div class="example-list">
                <div class="example-item">
                    <strong>displayName</strong>
                    <span th:text="${displayName}">MVC 학습자</span>
                </div>
                <div class="example-item" th:each="example : ${modelExamples}">
                    <strong th:text="${example}">displayName = MVC 학습자</strong>
                </div>
            </div>

            <p class="caption">View Resolver가 prefix/suffix를 적용한 템플릿 위치</p>
            <div class="path-box" th:text="${resolvedTemplatePath}">
                classpath:/templates/mission04/task02/view-resolver-demo.html
            </div>
        </article>
    </section>
</main>
</body>
</html>
```

</details>

### 4.6 `ViewResolverControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task02_view_resolver/ViewResolverControllerTest.java`
- 역할: 뷰 이름, 모델 데이터, 렌더링 결과 검증
- 상세 설명:
- 검증 시나리오 1: 기본 요청이 올바른 논리 뷰 이름과 모델 속성을 사용해 렌더링되는지 확인합니다.
- 검증 시나리오 2: `name` 파라미터를 전달했을 때 모델 값이 사용자 입력 기준으로 바뀌는지 확인합니다.
- 정상 흐름을 중심으로 보장하며, 뷰 이름과 HTML 결과 문자열까지 함께 검사해 템플릿 연결이 실제로 동작하는지 검증합니다.

<details>
<summary><code>ViewResolverControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver;

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
class ViewResolverControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기본 요청은 논리 뷰 이름과 모델 데이터를 담아 템플릿을 렌더링한다")
    void showViewResolverPage() throws Exception {
        mockMvc.perform(get("/mission04/task02/view-resolver"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task02/view-resolver-demo"))
                .andExpect(model().attribute("templateEngine", is("Thymeleaf")))
                .andExpect(model().attribute("logicalViewName", is("mission04/task02/view-resolver-demo")))
                .andExpect(model().attribute("resolvedTemplatePath",
                        is("classpath:/templates/mission04/task02/view-resolver-demo.html")))
                .andExpect(model().attribute("resolverFlow", hasSize(3)))
                .andExpect(model().attribute("modelExamples", hasSize(3)))
                .andExpect(content().string(containsString("View Resolver가 선택한 뷰")));
    }

    @Test
    @DisplayName("name 파라미터를 전달하면 모델 값이 바뀐 상태로 다시 렌더링한다")
    void showViewResolverPageWithCustomName() throws Exception {
        mockMvc.perform(get("/mission04/task02/view-resolver").param("name", "김스프링"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task02/view-resolver-demo"))
                .andExpect(model().attribute("displayName", is("김스프링")))
                .andExpect(content().string(containsString("김스프링님")))
                .andExpect(content().string(containsString("classpath:/templates/mission04/task02/view-resolver-demo.html")));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 ViewResolver

- 핵심: 컨트롤러가 반환한 논리 뷰 이름을 실제 `View` 객체나 템플릿 경로로 바꿔 주는 스프링 MVC 컴포넌트입니다.
- 왜 쓰는가: 컨트롤러가 템플릿 실제 경로를 직접 알지 않아도 되므로 웹 계층 결합도가 줄고, 뷰 기술 교체나 설정 변경이 쉬워집니다.
- 참고 링크:
  - Spring Framework View Resolution: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/viewresolver.html
  - Spring Framework `ViewResolver` API: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/ViewResolver.html

### 5.2 Spring Boot의 Thymeleaf 설정

- 핵심: `application.properties`의 `spring.thymeleaf.*` 속성으로 템플릿 prefix, suffix, 인코딩, 캐시 정책을 조정할 수 있습니다.
- 왜 쓰는가: 템플릿 경로 규칙과 개발 편의 설정을 코드 수정 없이 외부 설정으로 관리할 수 있어서 운영 환경과 개발 환경을 나누기 쉽습니다.
- 참고 링크:
  - Spring Boot Common Application Properties: https://docs.spring.io/spring-boot/appendix/application-properties/

### 5.3 Thymeleaf 템플릿 바인딩

- 핵심: `th:text`, `th:each` 같은 속성으로 `Model` 데이터를 HTML에 출력하고 반복 렌더링할 수 있습니다.
- 왜 쓰는가: 서버가 계산한 값을 화면 템플릿에 자연스럽게 연결할 수 있어서 JSP 없이도 읽기 쉬운 HTML 기반 서버 렌더링 페이지를 만들 수 있습니다.
- 참고 링크:
  - Thymeleaf + Spring 공식 튜토리얼: https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html
  - Spring Framework Thymeleaf 안내: https://docs.spring.io/spring-framework/reference/web/webmvc-view/mvc-thymeleaf.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 브라우저 또는 curl로 확인

```bash
curl http://localhost:8080/mission04/task02/view-resolver
curl "http://localhost:8080/mission04/task02/view-resolver?name=%EA%B9%80%EC%8A%A4%ED%94%84%EB%A7%81"
```

- 브라우저 확인 URL:
  - `http://localhost:8080/mission04/task02/view-resolver`
  - `http://localhost:8080/mission04/task02/view-resolver?name=김스프링`

### 6.3 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.ViewResolverControllerTest
```

- 예상 결과:
  - 애플리케이션 실행 시 `/mission04/task02/view-resolver` 요청에 HTML 화면이 반환됩니다.
  - 테스트 실행 시 `BUILD SUCCESSFUL`이 출력됩니다.

## 7. 결과 확인 방법

- 성공 기준:
  - 화면 상단에 `View Resolver 설정과 활용` 제목이 보입니다.
  - `View Resolver가 선택한 뷰` 영역에 `mission04/task02/view-resolver-demo`가 출력됩니다.
  - `View Resolver가 prefix/suffix를 적용한 템플릿 위치` 영역에 `classpath:/templates/mission04/task02/view-resolver-demo.html`가 출력됩니다.
  - `name=김스프링` 파라미터로 재호출하면 환영 메시지와 `displayName` 값이 `김스프링` 기준으로 바뀝니다.
- API/화면 확인 방법:
  - 브라우저로 `GET /mission04/task02/view-resolver` 호출
  - 브라우저로 `GET /mission04/task02/view-resolver?name=김스프링` 호출
  - 터미널에서는 `curl` 응답 HTML 안에 논리 뷰 이름과 템플릿 경로 문자열이 포함되는지 확인
  
## 8. 학습 내용

- 스프링 MVC에서 컨트롤러는 보통 템플릿의 실제 파일 경로를 직접 반환하지 않고, 논리 뷰 이름만 반환합니다. 이렇게 하면 컨트롤러는 요청 처리와 모델 구성에 집중하고, 어떤 템플릿 기술을 쓸지는 View Resolver와 설정이 담당합니다.
- 이번 태스크에서는 `mission04/task02/view-resolver-demo`라는 문자열이 그대로 화면 파일 경로가 되는 것이 아니라, `spring.thymeleaf.prefix`와 `spring.thymeleaf.suffix`가 더해져 `classpath:/templates/mission04/task02/view-resolver-demo.html`로 해석된다는 점을 코드와 화면 둘 다에서 확인할 수 있습니다.
- `Model`은 컨트롤러가 뷰에 전달하는 데이터 묶음입니다. 템플릿은 이 값을 `th:text`, `th:each`로 읽어 제목, 안내 문장, 리스트를 렌더링합니다. 즉, 컨트롤러가 데이터를 준비하고, View Resolver가 템플릿을 고르고, 템플릿 엔진이 최종 HTML을 만드는 흐름으로 이해하면 됩니다.
- `spring.thymeleaf.cache=false` 설정은 학습 단계에서 특히 유용합니다. 템플릿을 수정했을 때 서버 재시작 없이 변경 내용을 바로 확인하기 쉬워서 View Resolver와 템플릿 연결을 실험하기 편합니다.
