# 스프링 MVC 요청-응답 흐름 컴포넌트 요약

## 관찰 예시

- 예시 요청: `GET /mission04/task02/view-resolver?name=김스프링`
- 의도: View Resolver가 실제로 동작하는 기존 화면을 기준으로 요청부터 HTML 응답까지의 흐름을 설명한다.

## 주요 컴포넌트와 역할

| 컴포넌트 | 언제 등장하는가 | 역할 |
|---|---|---|
| DispatcherServlet | 스프링 MVC 요청이 들어오자마자 | 전체 요청 처리 흐름을 조정하는 프론트 컨트롤러 |
| HandlerMapping | 어떤 컨트롤러가 처리할지 찾을 때 | URL, HTTP 메서드, 애노테이션 조건에 맞는 핸들러를 찾는다 |
| HandlerAdapter | 실제 컨트롤러 메서드를 호출할 때 | 핸들러 종류에 맞는 호출 방식을 맞춰 준다 |
| Controller | 비즈니스 흐름을 시작할 때 | 요청값을 읽고 서비스 호출, Model 구성, 뷰 이름 반환을 담당한다 |
| Model | 컨트롤러 실행 결과를 뷰에 전달할 때 | 화면 렌더링에 필요한 데이터를 담는다 |
| ViewResolver | 논리 뷰 이름을 실제 뷰로 바꿀 때 | 예: `mission04/task02/view-resolver-demo`를 Thymeleaf 템플릿으로 해석한다 |
| View | 응답 직전에 | Model 데이터를 HTML에 반영해 최종 화면을 만든다 |

## 단계별 흐름

1. 클라이언트가 URL을 요청하면 WAS가 이를 받아 스프링 MVC의 시작점인 `DispatcherServlet`에 전달한다.
2. `DispatcherServlet`은 `HandlerMapping`에게 이 요청을 처리할 핸들러가 무엇인지 묻는다.
3. `HandlerMapping`은 `@RequestMapping`, `@GetMapping` 같은 매핑 정보를 보고 적절한 컨트롤러 메서드를 찾는다.
4. `DispatcherServlet`은 해당 핸들러를 실행할 수 있는 `HandlerAdapter`를 선택한다.
5. `HandlerAdapter`는 컨트롤러 메서드를 호출하고, 요청 파라미터 바인딩과 필요한 인자 준비를 돕는다.
6. 컨트롤러는 요청을 처리하고 `Model`에 화면 데이터를 담은 뒤 논리 뷰 이름을 반환한다.
7. `DispatcherServlet`은 반환된 논리 뷰 이름을 `ViewResolver`에 전달해 실제 뷰 객체를 찾는다.
8. `ViewResolver`는 prefix, suffix 같은 설정을 조합해 템플릿 위치를 해석한다.
9. `View`는 `Model` 데이터를 사용해 HTML을 렌더링한다.
10. `DispatcherServlet`은 렌더링된 결과를 HTTP 응답 본문에 담아 브라우저로 반환하고, 브라우저는 이를 화면에 표시한다.

## 예시 연결 포인트

- `task02`의 `ViewResolverController`는 논리 뷰 이름 `mission04/task02/view-resolver-demo`를 반환한다.
- `application.properties`의 Thymeleaf 설정은 이 이름을 `classpath:/templates/mission04/task02/view-resolver-demo.html`로 해석하게 만든다.
- 따라서 사용자는 단순히 URL을 호출하지만, 내부에서는 DispatcherServlet이 컨트롤러 실행과 ViewResolver 호출을 순서대로 조정한다.
