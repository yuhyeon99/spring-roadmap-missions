# 스프링 MVC: 필터와 인터셉터를 통한 요청 로깅

이 문서는 `mission-04-spring-mvc`의 `task-09-request-logging-filter-interceptor` 수행 결과를 정리한 보고서입니다. 특정 요청 경로에 대해 서블릿 필터와 스프링 MVC 인터셉터를 함께 적용해 요청 URL, 처리 시각, 처리 시간을 로그로 남기는 예제를 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-09-request-logging-filter-interceptor`
- 목표:
  - 필터와 인터셉터가 각각 어느 단계에서 동작하는지 로그로 구분해서 확인한다.
  - 특정 요청 경로에만 로깅을 적용해 공통 기능을 선택적으로 붙이는 방법을 정리한다.
  - `MockMvc` 테스트로 응답과 로그 메시지를 함께 검증한다.
- 엔드포인트: `GET /mission04/task09/logs/requests`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/controller/RequestLoggingController.java` | task09 전용 로깅 데모 요청을 처리하는 REST 컨트롤러 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/dto/RequestLoggingResponse.java` | 요청 정보와 처리 시각을 응답 본문으로 반환하는 DTO |
| Filter | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/filter/RequestLoggingFilter.java` | 서블릿 체인 앞뒤에서 URL, 시각, 상태 코드, 처리 시간을 기록하는 필터 |
| Interceptor | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/interceptor/RequestLoggingInterceptor.java` | 컨트롤러 실행 전후와 완료 시점 로그를 기록하는 인터셉터 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/config/RequestLoggingConfig.java` | 필터 URL 패턴 등록과 인터셉터 경로 매핑을 담당하는 설정 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/RequestLoggingControllerTest.java` | JSON 응답과 필터/인터셉터 로그 출력 여부를 검증하는 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `RequestLoggingController`에 `/mission04/task09/logs/requests` GET 엔드포인트를 만들고, 요청 메서드와 URI, 주제 파라미터를 응답 JSON으로 돌려주게 했습니다.
2. `RequestLoggingFilter`는 서블릿 필터 체인 진입 시점과 종료 시점에 각각 로그를 남깁니다. 여기서 요청 URL, 시작 시각, 최종 상태 코드, 전체 처리 시간을 기록합니다.
3. `RequestLoggingInterceptor`는 `preHandle`, `postHandle`, `afterCompletion` 세 구간에서 로그를 남기도록 구성했습니다. 덕분에 컨트롤러 실행 전, 컨트롤러 처리 직후, 뷰 렌더링 또는 응답 완료 직후를 구분해서 볼 수 있습니다.
4. `RequestLoggingConfig`는 필터를 `/mission04/task09/logs/*` 경로에만 등록하고, 인터셉터는 `/mission04/task09/logs/**` 패턴에만 적용했습니다. 그래서 다른 태스크 요청은 이 로깅 대상에 포함되지 않습니다.
5. `RequestLoggingControllerTest`는 `OutputCaptureExtension`으로 실제 로그를 캡처해 필터/인터셉터 메시지가 출력됐는지 검증합니다. 동시에 task09 바깥 경로는 로그가 남지 않는지도 확인합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `RequestLoggingController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/controller/RequestLoggingController.java`
- 역할: task09 전용 로깅 데모 요청을 처리하는 REST 컨트롤러
- 상세 설명:
- 기본 경로: `/mission04/task09/logs`
- HTTP 메서드/세부 경로: `GET /mission04/task09/logs/requests`
- `topic` 쿼리 파라미터를 받아 응답 JSON에 포함하고, 현재 요청 URI와 처리 시각을 함께 반환합니다. 이 요청이 필터와 인터셉터 로그를 확인하는 데모 진입점입니다.

<details>
<summary><code>RequestLoggingController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.dto.RequestLoggingResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission04/task09/logs")
public class RequestLoggingController {

    @GetMapping("/requests")
    public RequestLoggingResponse inspectRequest(
            @RequestParam(defaultValue = "filter-interceptor") String topic,
            HttpServletRequest request
    ) {
        return new RequestLoggingResponse(
                request.getMethod(),
                request.getRequestURI(),
                topic,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "필터와 인터셉터 로그를 함께 확인할 수 있습니다."
        );
    }
}
```

</details>

### 4.2 `RequestLoggingResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/dto/RequestLoggingResponse.java`
- 역할: 요청 정보와 처리 시각을 응답 본문으로 반환하는 DTO
- 상세 설명:
- 컨트롤러가 바로 JSON으로 반환하는 응답 모델입니다.
- `method`, `requestUri`, `topic`, `processedAt`, `message` 필드를 제공해 어떤 요청이 로깅 대상이었는지 응답만 봐도 알 수 있게 했습니다.
- 로그는 서버 콘솔에 남고, 이 DTO는 요청 자체가 정상 처리됐다는 확인용 데이터 역할을 합니다.

<details>
<summary><code>RequestLoggingResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.dto;

public class RequestLoggingResponse {

    private final String method;
    private final String requestUri;
    private final String topic;
    private final String processedAt;
    private final String message;

    public RequestLoggingResponse(String method, String requestUri, String topic, String processedAt, String message) {
        this.method = method;
        this.requestUri = requestUri;
        this.topic = topic;
        this.processedAt = processedAt;
        this.message = message;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getTopic() {
        return topic;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.3 `RequestLoggingFilter.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/filter/RequestLoggingFilter.java`
- 역할: 서블릿 체인 앞뒤에서 URL, 시각, 상태 코드, 처리 시간을 기록하는 필터
- 상세 설명:
- `OncePerRequestFilter`를 상속해 같은 요청에 대해 한 번만 실행되도록 구성했습니다.
- `doFilterInternal` 시작 지점에서 요청 메서드, URL, 시작 시각을 기록하고, `filterChain.doFilter`가 끝난 뒤에는 응답 상태 코드와 처리 시간을 기록합니다.
- 쿼리스트링이 있으면 URI 뒤에 붙여 로그에 남기므로 어떤 파라미터 조합으로 호출했는지 같이 확인할 수 있습니다.

<details>
<summary><code>RequestLoggingFilter.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestPath = buildRequestPath(request);

        request.setAttribute("task09FilterStartTime", startTime);

        log.info(
                "[Task09Filter][REQUEST] method={}, uri={}, startedAt={}",
                request.getMethod(),
                requestPath,
                LocalDateTime.now()
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "[Task09Filter][RESPONSE] method={}, uri={}, status={}, durationMs={}",
                    request.getMethod(),
                    requestPath,
                    response.getStatus(),
                    durationMs
            );
        }
    }

    private String buildRequestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }
}
```

</details>

### 4.4 `RequestLoggingInterceptor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/interceptor/RequestLoggingInterceptor.java`
- 역할: 컨트롤러 실행 전후와 완료 시점 로그를 기록하는 인터셉터
- 상세 설명:
- 핵심 공개 메서드: `preHandle`, `postHandle`, `afterCompletion`
- `preHandle`은 컨트롤러가 실행되기 직전에 호출되어 핸들러 메서드 이름과 요청 URL을 기록합니다.
- `postHandle`은 컨트롤러 실행 뒤에 호출되어 이번 요청이 뷰를 렌더링하는지, JSON처럼 바로 본문을 쓰는지 확인할 수 있게 합니다.
- `afterCompletion`은 전체 처리가 끝난 뒤 상태 코드, 처리 시간, 예외 발생 여부를 기록합니다.

<details>
<summary><code>RequestLoggingInterceptor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String INTERCEPTOR_START_TIME_ATTRIBUTE = "task09InterceptorStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(INTERCEPTOR_START_TIME_ATTRIBUTE, startTime);

        log.info(
                "[Task09Interceptor][PRE_HANDLE] method={}, uri={}, handler={}",
                request.getMethod(),
                buildRequestPath(request),
                resolveHandlerName(handler)
        );
        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable ModelAndView modelAndView
    ) {
        String viewName = modelAndView == null ? "response-body" : modelAndView.getViewName();
        log.info(
                "[Task09Interceptor][POST_HANDLE] method={}, uri={}, view={}, status={}",
                request.getMethod(),
                buildRequestPath(request),
                viewName,
                response.getStatus()
        );
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex
    ) {
        long startTime = (Long) request.getAttribute(INTERCEPTOR_START_TIME_ATTRIBUTE);
        long durationMs = System.currentTimeMillis() - startTime;
        String exceptionType = ex == null ? "none" : ex.getClass().getSimpleName();

        log.info(
                "[Task09Interceptor][AFTER_COMPLETION] method={}, uri={}, handler={}, status={}, durationMs={}, exception={}",
                request.getMethod(),
                buildRequestPath(request),
                resolveHandlerName(handler),
                response.getStatus(),
                durationMs,
                exceptionType
        );
    }

    private String buildRequestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    private String resolveHandlerName(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        }
        return handler.getClass().getSimpleName();
    }
}
```

</details>

### 4.5 `RequestLoggingConfig.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/config/RequestLoggingConfig.java`
- 역할: 필터 URL 패턴 등록과 인터셉터 경로 매핑을 담당하는 설정
- 상세 설명:
- `FilterRegistrationBean`으로 필터를 서블릿 레벨에 등록했습니다.
- 필터는 `/mission04/task09/logs/*`에만 적용하고, 인터셉터는 `addPathPatterns("/mission04/task09/logs/**")`로 MVC 핸들러 체인에 연결했습니다.
- 필터 순서를 가장 앞쪽으로 두어 요청이 스프링 MVC에 들어오기 전 단계부터 로그를 확인할 수 있게 했습니다.

<details>
<summary><code>RequestLoggingConfig.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.config;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.filter.RequestLoggingFilter;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.interceptor.RequestLoggingInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestLoggingConfig implements WebMvcConfigurer {

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(RequestLoggingFilter requestLoggingFilter) {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestLoggingFilter);
        registrationBean.setName("task09RequestLoggingFilter");
        registrationBean.addUrlPatterns("/mission04/task09/logs/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor())
                .addPathPatterns("/mission04/task09/logs/**");
    }
}
```

</details>

### 4.6 `RequestLoggingControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task09_request_logging_filter_interceptor/RequestLoggingControllerTest.java`
- 역할: JSON 응답과 필터/인터셉터 로그 출력 여부를 검증하는 테스트
- 상세 설명:
- 검증 시나리오 1: task09 데모 요청이 정상 JSON 응답을 반환하고, 필터/인터셉터 로그가 모두 출력되는지 확인합니다.
- 검증 시나리오 2: 다른 태스크 경로 요청은 task09 로깅 대상이 아니므로 관련 로그가 출력되지 않는지 확인합니다.
- 정상 흐름과 적용 범위 제한을 함께 검증해, 로깅 기능이 전체 앱에 무차별로 붙지 않았음을 보장합니다.

<details>
<summary><code>RequestLoggingControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class RequestLoggingControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("task09 요청은 필터와 인터셉터 로그를 함께 남긴다")
    void logsRequestWithFilterAndInterceptor(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/mission04/task09/logs/requests").param("topic", "request-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.requestUri").value("/mission04/task09/logs/requests"))
                .andExpect(jsonPath("$.topic").value("request-log"))
                .andExpect(jsonPath("$.message").value("필터와 인터셉터 로그를 함께 확인할 수 있습니다."));

        assertThat(output.getOut()).contains("[Task09Filter][REQUEST] method=GET, uri=/mission04/task09/logs/requests?topic=request-log");
        assertThat(output.getOut()).contains("[Task09Interceptor][PRE_HANDLE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, handler=RequestLoggingController.inspectRequest");
        assertThat(output.getOut()).contains("[Task09Interceptor][POST_HANDLE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, view=response-body, status=200");
        assertThat(output.getOut()).contains("[Task09Interceptor][AFTER_COMPLETION] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, handler=RequestLoggingController.inspectRequest, status=200");
        assertThat(output.getOut()).contains("[Task09Filter][RESPONSE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, status=200");
    }

    @Test
    @DisplayName("설정한 경로 밖의 요청은 task09 로깅 대상에서 제외된다")
    void doesNotLogOutsideConfiguredPath(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).doesNotContain("[Task09Filter]");
        assertThat(output.getOut()).doesNotContain("[Task09Interceptor]");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 Servlet Filter

- 핵심:
  - 필터는 서블릿 컨테이너 레벨에서 요청과 응답을 가로채 공통 기능을 적용하는 컴포넌트입니다.
  - 컨트롤러가 선택되기 전에도 실행되므로 로깅, 인증, 인코딩 같은 전처리에 자주 사용됩니다.
- 왜 쓰는가:
  - 특정 URL 패턴에 공통 처리를 붙이기 쉽고, 스프링 MVC 밖의 정적 리소스나 다른 서블릿까지 포함해 더 앞단에서 제어할 수 있습니다.
  - 요청 시작과 응답 종료를 한 쌍으로 감싸며 전체 처리 시간을 재기 좋습니다.
- 참고 링크:
  - Spring Framework Reference, Filters: https://docs.spring.io/spring-framework/reference/web/webmvc/filters.html
  - Jakarta EE Tutorial, Jakarta Servlet Filters: https://jakarta.ee/learn/docs/jakartaee-tutorial/current/web/servlets/servlets.html

### 5.2 OncePerRequestFilter

- 핵심:
  - `OncePerRequestFilter`는 같은 요청에 대해 필터가 중복 실행되지 않도록 도와주는 스프링 제공 베이스 클래스입니다.
  - `doFilterInternal`만 구현하면 되어 일반 `Filter`보다 코드가 단순합니다.
- 왜 쓰는가:
  - 포워드, 에러 디스패치 같은 추가 흐름에서 같은 로깅이 여러 번 찍히는 문제를 줄이기 쉽습니다.
  - 스프링 빈으로 등록했을 때 라이프사이클과 설정 연동이 자연스럽습니다.
- 참고 링크:
  - Spring Framework Reference, Filters: https://docs.spring.io/spring-framework/reference/web/webmvc/filters.html

### 5.3 HandlerInterceptor

- 핵심:
  - 인터셉터는 `HandlerMapping`이 컨트롤러를 찾은 뒤, 실제 핸들러 실행 전후에 동작하는 스프링 MVC 전용 확장 지점입니다.
  - `preHandle`, `postHandle`, `afterCompletion` 세 메서드로 세밀한 시점을 나눠 다룰 수 있습니다.
- 왜 쓰는가:
  - 컨트롤러 메서드 이름, `ModelAndView`, 예외 여부처럼 MVC 처리 맥락에 가까운 정보를 다루기 좋습니다.
  - 필터보다 뒤에서 동작하므로 컨트롤러 중심 로깅이나 화면 공통 처리에 적합합니다.
- 참고 링크:
  - Spring Framework Reference, Interception: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/handlermapping-interceptor.html
  - Spring Framework Reference, Interceptors Configuration: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/interceptors.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 방법

```bash
curl "http://localhost:8080/mission04/task09/logs/requests?topic=request-log"
```

예상 JSON 예시:

```json
{
  "method": "GET",
  "requestUri": "/mission04/task09/logs/requests",
  "topic": "request-log",
  "processedAt": "2026-03-20T14:30:15.123456",
  "message": "필터와 인터셉터 로그를 함께 확인할 수 있습니다."
}
```

### 6.3 테스트 실행

```bash
./gradlew test --tests "com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.RequestLoggingControllerTest"
```

예상 결과:

- task09 요청 테스트가 200 OK와 JSON 응답을 확인합니다.
- 콘솔 로그에 `[Task09Filter]`, `[Task09Interceptor]` 접두사가 포함된 메시지가 출력됩니다.
- task05 요청 테스트에서는 task09 로깅 접두사가 출력되지 않습니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - `GET /mission04/task09/logs/requests` 호출 시 200 OK와 JSON 응답이 반환됩니다.
  - 서버 콘솔에 필터 로그 2개, 인터셉터 로그 3개가 순서대로 출력됩니다.
  - `/mission04/task05/products` 같은 다른 경로 호출에서는 task09 전용 로그가 찍히지 않습니다.
- 로그 예제:

```text
[Task09Filter][REQUEST] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, startedAt=2026-03-20T14:30:15.120
[Task09Interceptor][PRE_HANDLE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, handler=RequestLoggingController.inspectRequest
[Task09Interceptor][POST_HANDLE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, view=response-body, status=200
[Task09Interceptor][AFTER_COMPLETION] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, handler=RequestLoggingController.inspectRequest, status=200, durationMs=7, exception=none
[Task09Filter][RESPONSE] method=GET, uri=/mission04/task09/logs/requests?topic=request-log, status=200, durationMs=9
```

- 스크린샷 파일명과 저장 위치:
  - API 응답 캡처: `task09-request-logging-response.png`, 저장 위치 `docs/mission-04-spring-mvc/task-09-request-logging-filter-interceptor/task09-request-logging-response.png`
  - 콘솔 로그 캡처: `task09-request-logging-console.png`, 저장 위치 `docs/mission-04-spring-mvc/task-09-request-logging-filter-interceptor/task09-request-logging-console.png`

## 8. 학습 내용

이번 태스크에서는 필터와 인터셉터가 비슷해 보여도 적용 위치가 다르다는 점이 가장 중요했습니다. 필터는 서블릿 체인에서 먼저 실행되기 때문에 요청이 스프링 MVC로 들어오기 전부터 감쌀 수 있고, 인터셉터는 이미 어떤 컨트롤러가 선택됐는지 아는 상태에서 동작합니다. 그래서 URL, 전체 처리 시간처럼 넓은 범위의 공통 관심사는 필터에 잘 맞고, 어떤 컨트롤러 메서드가 실행됐는지 같은 MVC 맥락 로그는 인터셉터에 더 잘 맞습니다.

또한 모든 요청에 무조건 공통 기능을 붙이지 않고, 경로 패턴으로 적용 범위를 제한하는 방법도 함께 확인했습니다. 실제 서비스에서는 요청 로깅이 많아지면 로그 양이 빠르게 커지므로, 필요한 경로에만 선택적으로 붙이는 설정이 중요합니다. 이번 구현처럼 필터는 `FilterRegistrationBean`으로, 인터셉터는 `addPathPatterns`로 범위를 제한하면 학습용 예제에서도 두 기술의 차이를 명확하게 확인할 수 있습니다.
