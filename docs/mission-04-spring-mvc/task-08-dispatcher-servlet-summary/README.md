# 스프링 MVC: DispatcherServlet의 동작 방식 요약

이 문서는 `mission-04-spring-mvc`의 `task-08-dispatcher-servlet-summary` 수행 결과를 정리한 보고서입니다. DispatcherServlet이 초기화 단계에서 어떤 전략 객체를 준비하는지, 요청을 받았을 때 어떤 순서로 핸들러를 찾고 실행하는지, 최종 응답을 어떻게 반환하는지를 요약 문서와 PDF로 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-08-dispatcher-servlet-summary`
- 목표:
  - DispatcherServlet의 초기화 과정과 요청 처리 흐름을 단계별로 정리한다.
  - HandlerMapping, HandlerAdapter, ViewResolver, HandlerExceptionResolver 같은 전략 객체의 역할을 구분해 요약한다.
  - 제출용 PDF 문서를 실제 파일로 생성하고, 원본 HTML/다이어그램과 함께 저장한다.
- 결과물:
  - 제출용 PDF: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Diagram | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-flow.mmd` | DispatcherServlet 흐름 Mermaid 원본 |
| Doc | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1.html` | 초기화와 전체 구조를 정리한 PDF 원본 HTML 1페이지 |
| Doc | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2.html` | 요청 처리/응답 반환을 정리한 PDF 원본 HTML 2페이지 |
| Asset | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1-preview.png` | 1페이지 HTML 미리보기 스크린샷 |
| Asset | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2-preview.png` | 2페이지 HTML 미리보기 스크린샷 |
| Doc | `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf` | 제출용 PDF 문서 |

## 3. 구현 단계와 주요 코드 해설

1. DispatcherServlet의 흐름을 `초기화 → 요청 수신 → HandlerMapping → HandlerAdapter → 컨트롤러 실행 → ViewResolver 또는 메시지 컨버터 → 최종 응답 반환` 순서로 요약했습니다.
2. 흐름을 한눈에 볼 수 있도록 `dispatcher-servlet-flow.mmd`에 Mermaid 다이어그램 원본을 만들었습니다.
3. PDF 제출용 문서는 한글 렌더링과 가독성을 고려해 HTML 2페이지로 나눴습니다. 1페이지는 초기화와 전체 구조, 2페이지는 요청 처리 단계와 전략 객체 역할을 정리합니다.
4. 생성한 HTML을 기반으로 미리보기 이미지를 만들고, 이를 묶어 `mission04-task08-dispatcher-servlet-summary.pdf`를 생성했습니다.
5. 문서에는 DispatcherServlet이 직접 비즈니스 로직을 처리하는 것이 아니라, 요청을 적절한 전략 객체와 연결하는 조정자라는 점을 핵심 메시지로 정리했습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `dispatcher-servlet-flow.mmd`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-flow.mmd`
- 역할: DispatcherServlet 흐름 Mermaid 원본
- 상세 설명:
- 초기화 단계부터 요청 수신, 핸들러 탐색, 컨트롤러 실행, 뷰 렌더링 또는 직접 응답 반환까지를 한 흐름으로 표현했습니다.
- 화면 렌더링이 필요한 경우와 REST 응답처럼 바로 본문을 쓰는 경우를 분기해서 보여 줍니다.
- README 설명과 PDF 요약 내용을 압축해 표현한 원본 다이어그램입니다.

<details>
<summary><code>dispatcher-servlet-flow.mmd</code> 전체 코드</summary>

```text
flowchart TD
    A["1. DispatcherServlet 초기화<br/>FrameworkServlet 초기화 후 WebApplicationContext 준비"] --> B["2. 전략 객체 탐색<br/>HandlerMapping, HandlerAdapter, ViewResolver, HandlerExceptionResolver 등"]
    B --> C["3. 클라이언트 요청 수신<br/>HttpServletRequest / HttpServletResponse 전달"]
    C --> D["4. HandlerMapping 조회<br/>요청을 처리할 핸들러 탐색"]
    D --> E["5. HandlerAdapter 선택<br/>핸들러 실행 방식 결정"]
    E --> F["6. 컨트롤러 실행<br/>ModelAndView 또는 응답 본문 생성"]
    F --> G{"7. View 렌더링 필요?"}
    G -- "예" --> H["8. ViewResolver로 논리 뷰 이름 해석"]
    H --> I["9. View 렌더링<br/>Model 데이터를 화면에 반영"]
    G -- "아니오" --> J["8. HttpMessageConverter 등으로 바로 응답 생성"]
    I --> K["10. DispatcherServlet이 최종 HTTP 응답 반환"]
    J --> K
```

</details>

### 4.2 `dispatcher-servlet-summary-page-1.html`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1.html`
- 역할: 초기화와 전체 구조를 정리한 PDF 원본 HTML 1페이지
- 상세 설명:
- DispatcherServlet의 정체, 초기화 시점 역할, 프론트 컨트롤러로서의 의미를 한 페이지로 정리했습니다.
- PDF 변환을 고려해 고정 폭 페이지와 큰 타이포그래피로 구성했습니다.
- 전체 흐름은 상자형 다이어그램으로 표현해 초기화와 요청 처리 준비 단계가 한눈에 들어오도록 했습니다.

<details>
<summary><code>dispatcher-servlet-summary-page-1.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DispatcherServlet Summary Page 1</title>
    <style>
        :root {
            --ink: #19324b;
            --muted: #586879;
            --paper: #ffffff;
            --line: #d7dee6;
            --accent: #b96516;
            --accent-soft: #f7e2ca;
            --bg: linear-gradient(145deg, #f4eee6, #dce7ef);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 24px;
            font-family: "Apple SD Gothic Neo", "Pretendard", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background: var(--bg);
        }

        .page {
            width: 1120px;
            min-height: 1580px;
            margin: 0 auto;
            padding: 40px;
            border-radius: 28px;
            background: rgba(255, 255, 255, 0.92);
            box-shadow: 0 22px 60px rgba(25, 50, 75, 0.12);
        }

        .eyebrow {
            display: inline-block;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 22px;
            font-weight: 800;
        }

        h1, h2, h3, p {
            margin: 0;
        }

        h1 {
            margin-top: 18px;
            font-size: 52px;
            line-height: 1.15;
        }

        .lead {
            margin-top: 18px;
            color: var(--muted);
            font-size: 24px;
            line-height: 1.7;
        }

        .section {
            margin-top: 34px;
        }

        .section-title {
            margin-bottom: 16px;
            font-size: 30px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .card, .flow-card {
            padding: 22px;
            border-radius: 22px;
            border: 1px solid var(--line);
            background: #fff;
        }

        .card h3, .flow-card h3 {
            font-size: 24px;
            margin-bottom: 10px;
        }

        .card p, .flow-card p, li {
            color: var(--muted);
            font-size: 21px;
            line-height: 1.7;
        }

        ul {
            margin: 12px 0 0;
            padding-left: 24px;
        }

        .diagram {
            margin-top: 18px;
            display: grid;
            gap: 14px;
        }

        .step {
            padding: 18px 20px;
            border-radius: 20px;
            border: 1px solid var(--line);
            background: #f9fbfc;
        }

        .step strong {
            display: block;
            margin-bottom: 8px;
            font-size: 23px;
        }

        .arrow {
            text-align: center;
            color: var(--accent);
            font-size: 28px;
            font-weight: 800;
        }

        .footer {
            margin-top: 30px;
            padding: 18px 20px;
            border-radius: 20px;
            background: #162435;
            color: #f7fafc;
            font-size: 20px;
            line-height: 1.7;
        }
    </style>
</head>
<body>
<section class="page">
    <span class="eyebrow">Mission04 Task08</span>
    <h1>DispatcherServlet 동작 방식 요약</h1>
    <p class="lead">
        DispatcherServlet은 스프링 MVC의 프론트 컨트롤러입니다.
        모든 웹 요청을 먼저 받아서 어떤 컨트롤러를 실행할지 결정하고,
        필요하면 뷰를 렌더링해 최종 HTTP 응답까지 조정합니다.
    </p>

    <div class="section">
        <h2 class="section-title">1. 초기화 시점에 하는 일</h2>
        <div class="grid">
            <article class="card">
                <h3>FrameworkServlet 초기화</h3>
                <p>
                    톰캣 같은 서블릿 컨테이너가 DispatcherServlet을 생성하면,
                    내부적으로 WebApplicationContext를 준비하고 스프링 MVC 전용 빈을 찾을 준비를 합니다.
                </p>
            </article>
            <article class="card">
                <h3>전략 객체 탐색</h3>
                <p>
                    HandlerMapping, HandlerAdapter, ViewResolver, HandlerExceptionResolver 같은 전략 객체를 컨텍스트에서 찾거나 기본 구현을 사용합니다.
                </p>
            </article>
        </div>
    </div>

    <div class="section">
        <h2 class="section-title">2. 왜 Front Controller 인가</h2>
        <div class="grid">
            <article class="card">
                <h3>공통 흐름 중앙화</h3>
                <ul>
                    <li>요청 매핑 검색</li>
                    <li>컨트롤러 호출 방식 결정</li>
                    <li>예외 처리와 뷰 해석</li>
                    <li>응답 생성 규칙 통일</li>
                </ul>
            </article>
            <article class="card">
                <h3>개별 컨트롤러 책임 축소</h3>
                <p>
                    컨트롤러는 비즈니스 처리와 모델 구성에 집중하고,
                    요청 라우팅, 바인딩, 렌더링 세부 과정은 DispatcherServlet과 전략 객체가 담당합니다.
                </p>
            </article>
        </div>
    </div>

    <div class="section">
        <h2 class="section-title">3. 전체 흐름 한눈에 보기</h2>
        <div class="diagram">
            <div class="step">
                <strong>1. 초기화</strong>
                FrameworkServlet이 WebApplicationContext를 준비하고 MVC 전략 객체를 찾습니다.
            </div>
            <div class="arrow">↓</div>
            <div class="step">
                <strong>2. 요청 수신</strong>
                클라이언트 요청이 서블릿 컨테이너를 거쳐 DispatcherServlet에 도착합니다.
            </div>
            <div class="arrow">↓</div>
            <div class="step">
                <strong>3. 핸들러 탐색</strong>
                HandlerMapping이 현재 요청을 처리할 컨트롤러를 찾습니다.
            </div>
            <div class="arrow">↓</div>
            <div class="step">
                <strong>4. 컨트롤러 실행</strong>
                HandlerAdapter가 컨트롤러 메서드를 호출합니다.
            </div>
            <div class="arrow">↓</div>
            <div class="step">
                <strong>5. 응답 선택</strong>
                뷰 렌더링이 필요하면 ViewResolver로 가고, REST 응답이면 본문을 바로 씁니다.
            </div>
        </div>
    </div>

    <div class="footer">
        핵심: DispatcherServlet은 직접 모든 비즈니스 로직을 수행하지 않습니다.
        대신 "누가 처리할지", "어떻게 실행할지", "어떤 방식으로 응답할지"를 조정하는 중심 관리자 역할을 맡습니다.
    </div>
</section>
</body>
</html>
```

</details>

### 4.3 `dispatcher-servlet-summary-page-2.html`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2.html`
- 역할: 요청 처리/응답 반환을 정리한 PDF 원본 HTML 2페이지
- 상세 설명:
- HandlerMapping, HandlerAdapter, HandlerExceptionResolver, ViewResolver, HttpMessageConverter가 요청 처리 중 어떤 위치에서 등장하는지 정리했습니다.
- 화면 렌더링 응답과 REST 응답의 분기 지점을 표와 단계 목록으로 설명합니다.
- 첫 페이지가 전체 구조 중심이라면, 두 번째 페이지는 실제 요청 처리 메커니즘 중심입니다.

<details>
<summary><code>dispatcher-servlet-summary-page-2.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DispatcherServlet Summary Page 2</title>
    <style>
        :root {
            --ink: #17283e;
            --muted: #5c6a7a;
            --line: #d8dfe6;
            --accent: #0f766e;
            --accent-soft: #d8f0ec;
            --bg: linear-gradient(145deg, #eef4ef, #dbe7ef);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 24px;
            font-family: "Apple SD Gothic Neo", "Pretendard", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background: var(--bg);
        }

        .page {
            width: 1120px;
            min-height: 1580px;
            margin: 0 auto;
            padding: 40px;
            border-radius: 28px;
            background: rgba(255, 255, 255, 0.94);
            box-shadow: 0 22px 60px rgba(23, 40, 62, 0.12);
        }

        .eyebrow {
            display: inline-block;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 22px;
            font-weight: 800;
        }

        h1, h2, h3, p {
            margin: 0;
        }

        h1 {
            margin-top: 18px;
            font-size: 46px;
            line-height: 1.15;
        }

        .lead {
            margin-top: 18px;
            color: var(--muted);
            font-size: 23px;
            line-height: 1.7;
        }

        .section {
            margin-top: 32px;
        }

        .section-title {
            margin-bottom: 16px;
            font-size: 29px;
        }

        .steps {
            display: grid;
            gap: 14px;
        }

        .step {
            padding: 20px 22px;
            border-radius: 22px;
            border: 1px solid var(--line);
            background: #fff;
        }

        .step strong {
            display: block;
            margin-bottom: 8px;
            font-size: 24px;
        }

        .step p, .table td, .table th, .note {
            color: var(--muted);
            font-size: 20px;
            line-height: 1.7;
        }

        .table {
            width: 100%;
            border-collapse: collapse;
            overflow: hidden;
            border-radius: 22px;
            border-style: hidden;
            box-shadow: 0 0 0 1px var(--line);
            background: #fff;
        }

        .table th, .table td {
            padding: 16px 18px;
            border: 1px solid var(--line);
            text-align: left;
            vertical-align: top;
        }

        .table th {
            color: var(--ink);
            background: #f7fafb;
            font-size: 20px;
        }

        .note {
            margin-top: 18px;
            padding: 18px 20px;
            border-radius: 20px;
            background: #162435;
            color: #f8fafc;
        }
    </style>
</head>
<body>
<section class="page">
    <span class="eyebrow">Mission04 Task08</span>
    <h1>요청 처리부터 응답 반환까지</h1>
    <p class="lead">
        DispatcherServlet은 요청이 들어오면 매핑 조회, 어댑터 선택, 컨트롤러 실행,
        예외 처리, 뷰 렌더링 또는 응답 본문 작성까지를 순서대로 조정합니다.
    </p>

    <div class="section">
        <h2 class="section-title">1. 요청 처리 단계</h2>
        <div class="steps">
            <div class="step">
                <strong>1) HandlerMapping 조회</strong>
                <p>URL, HTTP 메서드, 애노테이션 조건을 바탕으로 어떤 핸들러가 현재 요청을 처리할지 찾습니다.</p>
            </div>
            <div class="step">
                <strong>2) HandlerAdapter 선택</strong>
                <p>찾은 핸들러를 실제로 실행할 수 있는 어댑터를 선택해, 컨트롤러 메서드 인자 준비와 호출 방식을 맞춥니다.</p>
            </div>
            <div class="step">
                <strong>3) 컨트롤러 실행</strong>
                <p>컨트롤러는 비즈니스 로직을 호출하고 ModelAndView, 논리 뷰 이름, ResponseEntity, JSON 본문 같은 결과를 반환합니다.</p>
            </div>
            <div class="step">
                <strong>4) 예외 처리 분기</strong>
                <p>실행 중 예외가 발생하면 HandlerExceptionResolver 또는 @ExceptionHandler, @ControllerAdvice 흐름으로 연결됩니다.</p>
            </div>
            <div class="step">
                <strong>5) ViewResolver 또는 메시지 컨버터</strong>
                <p>HTML 화면이면 ViewResolver가 논리 뷰 이름을 실제 View로 바꾸고, REST 응답이면 HttpMessageConverter가 본문을 바로 작성합니다.</p>
            </div>
        </div>
    </div>

    <div class="section">
        <h2 class="section-title">2. 주요 전략 객체 역할</h2>
        <table class="table">
            <thead>
            <tr>
                <th>컴포넌트</th>
                <th>DispatcherServlet과의 관계</th>
                <th>왜 필요한가</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>HandlerMapping</td>
                <td>요청을 어떤 핸들러로 보낼지 찾는다</td>
                <td>라우팅 규칙을 DispatcherServlet 바깥으로 분리한다</td>
            </tr>
            <tr>
                <td>HandlerAdapter</td>
                <td>선택된 핸들러를 실제로 실행한다</td>
                <td>컨트롤러 종류가 달라도 같은 흐름으로 처리할 수 있다</td>
            </tr>
            <tr>
                <td>HandlerExceptionResolver</td>
                <td>예외를 HTTP 응답이나 오류 화면으로 바꾼다</td>
                <td>컨트롤러마다 try-catch를 반복하지 않게 한다</td>
            </tr>
            <tr>
                <td>ViewResolver</td>
                <td>논리 뷰 이름을 실제 View로 해석한다</td>
                <td>컨트롤러가 템플릿 경로를 직접 알 필요가 없다</td>
            </tr>
            <tr>
                <td>HttpMessageConverter</td>
                <td>객체를 JSON/XML/문자열 응답으로 직렬화한다</td>
                <td>REST API 응답을 뷰 없이 바로 만들 수 있다</td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h2 class="section-title">3. 정리 포인트</h2>
        <div class="steps">
            <div class="step">
                <strong>초기화 관점</strong>
                <p>DispatcherServlet은 시작 시점에 MVC 전략 객체를 준비해 요청이 들어왔을 때 즉시 사용할 수 있는 상태가 됩니다.</p>
            </div>
            <div class="step">
                <strong>요청 처리 관점</strong>
                <p>DispatcherServlet은 직접 비즈니스 로직을 수행하지 않고, 적절한 전략 객체를 선택하고 호출 순서를 조정합니다.</p>
            </div>
            <div class="step">
                <strong>응답 관점</strong>
                <p>결과가 화면이면 뷰 렌더링으로, API면 메시지 컨버터로 분기해 최종 HTTP 응답을 완성합니다.</p>
            </div>
        </div>
        <div class="note">
            한 줄 요약: DispatcherServlet은 스프링 MVC 요청 처리의 "중앙 관제실"입니다.
            초기화 단계에서 전략 객체를 준비하고, 런타임에는 요청을 적절한 컨트롤러와 응답 방식으로 연결합니다.
        </div>
    </div>
</section>
</body>
</html>
```

</details>

### 4.4 `mission04-task08-dispatcher-servlet-summary.pdf`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf`
- 역할: 제출용 PDF 문서
- 상세 설명:
- 2페이지 HTML 요약 문서를 PDF로 묶은 최종 제출 결과물입니다.
- 한글 렌더링 안정성을 위해 HTML 미리보기 이미지를 기반으로 생성했습니다.
- 바이너리 파일이므로 전체 코드 토글 대신 생성 결과와 경로를 문서에 명시했습니다.

<details>
<summary><code>mission04-task08-dispatcher-servlet-summary.pdf</code> 생성 정보</summary>

```text
생성 결과물 경로:
docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf

구성 페이지:
1. dispatcher-servlet-summary-page-1.html
2. dispatcher-servlet-summary-page-2.html
```

</details>

### 4.5 `dispatcher-servlet-summary-page-1-preview.png`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1-preview.png`
- 역할: 초기화와 전체 구조 요약 페이지의 미리보기 스크린샷
- 상세 설명:
- `dispatcher-servlet-summary-page-1.html`을 기반으로 생성한 PNG 미리보기입니다.
- PDF 제출 전에 1페이지 레이아웃과 한글 렌더링 상태를 확인하는 용도로 사용했습니다.
- 바이너리 이미지 파일이므로 전체 코드 대신 산출물 경로와 용도를 문서에 남깁니다.

<details>
<summary><code>dispatcher-servlet-summary-page-1-preview.png</code> 생성 정보</summary>

```text
원본 HTML:
docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1.html

생성 산출물:
docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1-preview.png
```

</details>

### 4.6 `dispatcher-servlet-summary-page-2-preview.png`

- 파일 경로: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2-preview.png`
- 역할: 요청 처리/응답 반환 요약 페이지의 미리보기 스크린샷
- 상세 설명:
- `dispatcher-servlet-summary-page-2.html`을 기반으로 생성한 PNG 미리보기입니다.
- 요청 매핑, 핸들러 실행, 응답 분기 설명이 한 장에 들어가는지 확인할 수 있습니다.
- 바이너리 이미지 파일이므로 전체 코드 대신 산출물 경로와 용도를 문서에 남깁니다.

<details>
<summary><code>dispatcher-servlet-summary-page-2-preview.png</code> 생성 정보</summary>

```text
원본 HTML:
docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2.html

생성 산출물:
docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2-preview.png
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `DispatcherServlet`

- 핵심: 스프링 MVC의 핵심 프론트 컨트롤러로서 모든 웹 요청을 가장 먼저 받아 전체 흐름을 조정합니다.
- 왜 쓰는가: 라우팅, 예외 처리, 뷰 해석, 응답 생성 규칙을 한 지점에 모아 공통 처리 구조를 만들 수 있습니다.
- 참고 링크:
  - Spring Framework DispatcherServlet: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html
  - Spring Framework `DispatcherServlet` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html

### 5.2 `FrameworkServlet`과 초기화

- 핵심: DispatcherServlet은 `FrameworkServlet`을 상속하며, 초기화 시점에 WebApplicationContext와 MVC 전략 객체를 준비합니다.
- 왜 쓰는가: 요청이 들어오기 전부터 필요한 전략 객체를 확보해 두어, 런타임 요청 처리 흐름을 일관되게 유지할 수 있습니다.
- 참고 링크:
  - Spring Framework `FrameworkServlet` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/FrameworkServlet.html
  - Spring MVC Special Bean Types: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/special-bean-types.html

### 5.3 `HandlerMapping`, `HandlerAdapter`, `ViewResolver`

- 핵심: HandlerMapping은 핸들러를 찾고, HandlerAdapter는 실행 방식을 맞추고, ViewResolver는 논리 뷰 이름을 실제 뷰로 해석합니다.
- 왜 쓰는가: DispatcherServlet이 모든 세부 구현을 직접 알지 않아도, 전략 객체 조합으로 확장 가능하고 일관된 MVC 흐름을 구성할 수 있습니다.
- 참고 링크:
  - Handler Mappings: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/handlermapping.html
  - Handler Adapters: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/handleradapter.html
  - View Resolution: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/viewresolver.html

## 6. 실행·검증 방법

### 6.1 문서 파일 확인

```bash
ls docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary
```

### 6.2 PDF 파일 존재 여부 확인

```bash
file docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf
```

- 예상 결과:
  - `PDF document` 형태로 파일 타입이 출력됩니다.

### 6.3 원본 HTML 확인

```bash
open docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1.html
open docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2.html
```

- 예상 결과:
  - 초기화/전체 구조와 요청 처리/응답 반환이 각각 1장씩 표시됩니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - PDF 문서가 실제 파일로 생성되어 있습니다.
  - 1페이지에는 초기화와 전체 구조, 2페이지에는 요청 처리와 응답 반환 요약이 포함됩니다.
  - DispatcherServlet이 단순 서블릿이 아니라 MVC 전략 객체를 조정하는 중심 컴포넌트라는 메시지가 드러납니다.
- 결과 확인 파일:
  - 제출용 PDF: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/mission04-task08-dispatcher-servlet-summary.pdf`
  - 미리보기 이미지 1: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-1-preview.png`
  - 미리보기 이미지 2: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/dispatcher-servlet-summary-page-2-preview.png`
- 스크린샷 파일명과 저장 위치:
  - `dispatcher-servlet-summary-page-1-preview.png`
  - `dispatcher-servlet-summary-page-2-preview.png`
  - 저장 위치: `docs/mission-04-spring-mvc/task-08-dispatcher-servlet-summary/`

## 8. 학습 내용

- DispatcherServlet을 이해할 때 가장 중요한 점은 "모든 일을 직접 처리하는 객체"가 아니라 "요청 처리 흐름을 조정하는 객체"라는 것입니다. 실제 매핑 탐색, 핸들러 실행, 예외 처리, 뷰 해석은 각각 다른 전략 객체가 맡습니다.
- 초기화 단계에서 전략 객체를 준비해 두기 때문에, 요청이 들어왔을 때 DispatcherServlet은 어떤 순서로 어떤 컴포넌트를 호출해야 하는지만 결정하면 됩니다. 이 구조 덕분에 스프링 MVC는 확장성과 일관성을 동시에 확보합니다.
- 요청 결과가 화면인지, API 응답인지에 따라 마지막 단계가 달라진다는 점도 중요합니다. 화면이면 ViewResolver와 View 렌더링으로 가고, REST API면 HttpMessageConverter가 바로 응답 본문을 작성합니다.
- 이번 태스크를 PDF 문서로 정리하면서, DispatcherServlet의 흐름을 "초기화"와 "요청 처리"로 나눠 보는 것이 이해에 도움이 된다는 점을 확인할 수 있었습니다.
