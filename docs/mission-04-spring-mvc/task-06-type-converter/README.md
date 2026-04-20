# 스프링 MVC: 타입 변환기를 통한 사용자 입력 데이터 변환

이 문서는 `mission-04-spring-mvc`의 `task-06-type-converter` 수행 결과를 정리한 보고서입니다. `yyyyMMdd` 형식의 문자열 요청 파라미터를 `LocalDate`로 바꾸는 커스텀 타입 변환기를 등록하고, 변환된 날짜를 일정 조회 예제에서 사용하는 흐름을 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-06-type-converter`
- 목표:
  - 문자열 사용자 입력을 원하는 자바 타입으로 변환하는 커스텀 `Converter`를 만든다.
  - `WebMvcConfigurer`에서 타입 변환기를 등록해 스프링 MVC 요청 바인딩 과정에 연결한다.
  - 날짜 문자열이 `LocalDate`로 변환되는 정상 흐름과 잘못된 형식에서 `400 Bad Request`가 반환되는 예외 흐름을 함께 검증한다.
- 엔드포인트:
  - `GET /mission04/task06/schedules?date=20260319`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Converter | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/converter/StringToLocalDateConverter.java` | `yyyyMMdd` 문자열을 `LocalDate`로 변환하는 커스텀 타입 변환기 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/config/TypeConverterWebConfig.java` | 변환기를 스프링 MVC FormatterRegistry에 등록 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/controller/ScheduleLookupController.java` | 변환된 `LocalDate`를 요청 파라미터로 받아 일정 조회 응답 반환 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/service/ScheduleLookupService.java` | 날짜 기준 일정 메시지와 요일 정보 생성 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/dto/ScheduleLookupResponse.java` | 일정 조회 응답 JSON 구조 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/ScheduleLookupControllerTest.java` | 날짜 변환 성공과 잘못된 형식의 400 응답 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `StringToLocalDateConverter`에서 입력 문자열을 `DateTimeFormatter.ofPattern("yyyyMMdd")`로 파싱하도록 구현했습니다. 형식이 맞지 않으면 `IllegalArgumentException`을 던져 잘못된 입력을 명확히 구분합니다.
2. `TypeConverterWebConfig`는 `WebMvcConfigurer`를 구현하고 `addFormatters()`에서 커스텀 변환기를 등록합니다. 이 설정으로 스프링 MVC가 요청 파라미터를 바인딩할 때 해당 변환기를 사용할 수 있습니다.
3. `ScheduleLookupController`는 `@RequestParam("date") LocalDate date` 형태로 날짜를 받습니다. 컨트롤러는 문자열을 직접 파싱하지 않고, 이미 변환된 `LocalDate`를 바로 사용합니다.
4. `ScheduleLookupService`는 변환된 날짜의 요일과 예시 일정 메시지를 계산해 응답 DTO를 만듭니다. 날짜 변환 이후 비즈니스 로직은 `LocalDate` 기준으로 단순하게 처리됩니다.
5. `ScheduleLookupControllerTest`는 `20260319`처럼 올바른 형식은 `200 OK`와 JSON 응답을, `2026-03-19`처럼 변환기 형식과 다른 입력은 `400 Bad Request`를 반환하는지 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `StringToLocalDateConverter.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/converter/StringToLocalDateConverter.java`
- 역할: `yyyyMMdd` 문자열을 `LocalDate`로 변환하는 커스텀 타입 변환기
- 상세 설명:
- 스프링의 `Converter<String, LocalDate>`를 구현해 문자열 입력을 날짜 객체로 변환합니다.
- 공백 입력은 `null`로 처리하고, 형식이 맞지 않으면 `IllegalArgumentException`을 발생시켜 요청 바인딩 실패로 연결합니다.
- 예시 입력 `20260319`는 `LocalDate.of(2026, 3, 19)`로 해석됩니다.

<details>
<summary><code>StringToLocalDateConverter.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public LocalDate convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        try {
            return LocalDate.parse(source.trim(), FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("날짜는 yyyyMMdd 형식이어야 합니다. 예: 20260319");
        }
    }
}
```

</details>

### 4.2 `TypeConverterWebConfig.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/config/TypeConverterWebConfig.java`
- 역할: 변환기를 스프링 MVC FormatterRegistry에 등록
- 상세 설명:
- `WebMvcConfigurer`의 `addFormatters()`를 오버라이드해 커스텀 변환기를 등록합니다.
- 이 설정이 있어야 `@RequestParam LocalDate` 바인딩 시 스프링 MVC가 직접 문자열을 `LocalDate`로 바꿀 수 있습니다.
- 컨트롤러 코드와 변환 로직을 분리해 재사용성과 가독성을 높였습니다.

<details>
<summary><code>TypeConverterWebConfig.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.config;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.converter.StringToLocalDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TypeConverterWebConfig implements WebMvcConfigurer {

    private final StringToLocalDateConverter stringToLocalDateConverter;

    public TypeConverterWebConfig(StringToLocalDateConverter stringToLocalDateConverter) {
        this.stringToLocalDateConverter = stringToLocalDateConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateConverter);
    }
}
```

</details>

### 4.3 `ScheduleLookupController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/controller/ScheduleLookupController.java`
- 역할: 변환된 `LocalDate`를 요청 파라미터로 받아 일정 조회 응답 반환
- 상세 설명:
- 기본 경로: `/mission04/task06/schedules`
- HTTP 메서드/세부 경로: `GET /mission04/task06/schedules`
- `date` 요청 파라미터를 `LocalDate` 타입으로 직접 받습니다. 컨트롤러 안에는 문자열 파싱 코드가 없습니다.
- 변환이 성공하면 `ScheduleLookupService`를 호출해 일정 응답을 만들고, 실패하면 스프링 MVC가 `400 Bad Request`를 반환합니다.

<details>
<summary><code>ScheduleLookupController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto.ScheduleLookupResponse;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.service.ScheduleLookupService;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission04/task06/schedules")
public class ScheduleLookupController {

    private final ScheduleLookupService scheduleLookupService;

    public ScheduleLookupController(ScheduleLookupService scheduleLookupService) {
        this.scheduleLookupService = scheduleLookupService;
    }

    @GetMapping
    public ScheduleLookupResponse findByDate(@RequestParam("date") LocalDate date) {
        return scheduleLookupService.findSchedule(date);
    }
}
```

</details>

### 4.4 `ScheduleLookupService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/service/ScheduleLookupService.java`
- 역할: 날짜 기준 일정 메시지와 요일 정보 생성
- 상세 설명:
- 핵심 공개 메서드: `findSchedule(LocalDate requestedDate)`
- `LocalDate`를 기준으로 요일 이름과 예시 일정 메시지를 조회해 응답 DTO를 생성합니다.
- 컨트롤러가 문자열 변환과 비즈니스 로직을 동시에 처리하지 않도록 역할을 분리했습니다.

<details>
<summary><code>ScheduleLookupService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto.ScheduleLookupResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ScheduleLookupService {

    private static final Map<DayOfWeek, String> DAY_NAMES = Map.of(
            DayOfWeek.MONDAY, "월요일",
            DayOfWeek.TUESDAY, "화요일",
            DayOfWeek.WEDNESDAY, "수요일",
            DayOfWeek.THURSDAY, "목요일",
            DayOfWeek.FRIDAY, "금요일",
            DayOfWeek.SATURDAY, "토요일",
            DayOfWeek.SUNDAY, "일요일"
    );

    private static final Map<DayOfWeek, String> AGENDAS = Map.of(
            DayOfWeek.MONDAY, "주간 계획을 정리하는 날입니다.",
            DayOfWeek.TUESDAY, "컨트롤러 요청 흐름을 점검하는 날입니다.",
            DayOfWeek.WEDNESDAY, "데이터 바인딩과 검증을 복습하는 날입니다.",
            DayOfWeek.THURSDAY, "타입 변환기 설정을 실습하는 날입니다.",
            DayOfWeek.FRIDAY, "예외 처리와 응답 형식을 정리하는 날입니다.",
            DayOfWeek.SATURDAY, "개인 학습 과제를 진행하는 날입니다.",
            DayOfWeek.SUNDAY, "이번 주 학습 내용을 회고하는 날입니다."
    );

    public ScheduleLookupResponse findSchedule(LocalDate requestedDate) {
        DayOfWeek dayOfWeek = requestedDate.getDayOfWeek();
        return new ScheduleLookupResponse(
                requestedDate,
                DAY_NAMES.get(dayOfWeek),
                AGENDAS.get(dayOfWeek),
                "query parameter 문자열이 LocalDate로 변환된 뒤 일정 조회에 사용되었습니다."
        );
    }
}
```

</details>

### 4.5 `ScheduleLookupResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/dto/ScheduleLookupResponse.java`
- 역할: 일정 조회 응답 JSON 구조
- 상세 설명:
- 요청 날짜, 요일, 일정 설명, 변환 메시지를 응답 본문으로 전달합니다.
- Jackson이 `LocalDate`를 ISO 형식 문자열(`2026-03-19`)로 직렬화해 JSON 응답에 포함합니다.
- 응답 구조를 별도 DTO로 분리해 컨트롤러와 서비스가 반환 형식에 직접 결합되지 않게 했습니다.

<details>
<summary><code>ScheduleLookupResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto;

import java.time.LocalDate;

public class ScheduleLookupResponse {

    private final LocalDate requestedDate;
    private final String dayOfWeek;
    private final String agenda;
    private final String message;

    public ScheduleLookupResponse(LocalDate requestedDate, String dayOfWeek, String agenda, String message) {
        this.requestedDate = requestedDate;
        this.dayOfWeek = dayOfWeek;
        this.agenda = agenda;
        this.message = message;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getAgenda() {
        return agenda;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.6 `ScheduleLookupControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task06_type_converter/ScheduleLookupControllerTest.java`
- 역할: 날짜 변환 성공과 잘못된 형식의 400 응답 검증
- 상세 설명:
- 검증 시나리오 1: `20260319`가 `LocalDate`로 변환되어 `requestedDate`, `dayOfWeek`, `agenda`가 정상 응답에 포함되는지 확인합니다.
- 검증 시나리오 2: 변환기 형식과 다른 `2026-03-19` 입력 시 `400 Bad Request`가 반환되는지 확인합니다.
- `WebApplicationContext` 기반 `MockMvc`를 사용해 실제 스프링 MVC 설정과 변환기 등록 흐름을 반영했습니다.

<details>
<summary><code>ScheduleLookupControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ScheduleLookupControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("yyyyMMdd 형식의 문자열은 LocalDate 로 변환되어 일정 조회에 사용된다")
    void convertsStringQueryParameterToLocalDate() throws Exception {
        mockMvc.perform(get("/mission04/task06/schedules").param("date", "20260319"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedDate").value("2026-03-19"))
                .andExpect(jsonPath("$.dayOfWeek").value("목요일"))
                .andExpect(jsonPath("$.agenda").value("타입 변환기 설정을 실습하는 날입니다."));
    }

    @Test
    @DisplayName("지정한 형식이 아닌 날짜 문자열은 400 Bad Request 를 반환한다")
    void returnsBadRequestWhenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/mission04/task06/schedules").param("date", "2026-03-19"))
                .andExpect(status().isBadRequest());
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `Converter<S, T>`

- 핵심: 한 타입의 값을 다른 타입으로 바꾸는 스프링 변환 인터페이스입니다.
- 왜 쓰는가: 요청 파라미터나 폼 입력값이 문자열로 들어오더라도, 컨트롤러에서는 원하는 자바 타입으로 직접 받을 수 있게 해 줍니다.
- 참고 링크:
  - Spring Framework Type Conversion: https://docs.spring.io/spring-framework/reference/core/validation/convert.html
  - Spring Framework `Converter` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/converter/Converter.html

### 5.2 `WebMvcConfigurer#addFormatters`

- 핵심: 스프링 MVC가 사용하는 `FormatterRegistry`에 커스텀 변환기나 포매터를 등록하는 설정 지점입니다.
- 왜 쓰는가: 특정 컨트롤러 안에서만 문자열을 파싱하는 대신, 애플리케이션 전반의 요청 바인딩 과정에 공통 규칙을 연결할 수 있습니다.
- 참고 링크:
  - Spring Framework MVC Config: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/conversion.html
  - Spring Framework `WebMvcConfigurer` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html

### 5.3 `LocalDate` 바인딩과 400 응답

- 핵심: 변환기에 실패하면 스프링 MVC는 요청 파라미터를 목표 타입으로 바인딩할 수 없다고 판단해 `400 Bad Request`를 반환합니다.
- 왜 쓰는가: 잘못된 사용자 입력을 초기에 걸러 컨트롤러 내부 로직까지 잘못된 값이 들어가지 않도록 막을 수 있습니다.
- 참고 링크:
  - Spring Framework Type Conversion and Data Binding: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/typeconversion.html
  - Java `LocalDate` Javadoc: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/LocalDate.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 정상 날짜 형식 요청

```bash
curl -i "http://localhost:8080/mission04/task06/schedules?date=20260319"
```

- 예상 결과:
  - 상태 코드 `200 OK`
  - 응답 JSON의 `requestedDate` 값이 `2026-03-19`로 반환
  - `dayOfWeek`는 `목요일`, `agenda`는 목요일 일정 메시지 반환

### 6.3 잘못된 날짜 형식 요청

```bash
curl -i "http://localhost:8080/mission04/task06/schedules?date=2026-03-19"
```

- 예상 결과:
  - 상태 코드 `400 Bad Request`
  - 변환기 형식과 맞지 않아 컨트롤러 진입 전에 요청이 거부됨

### 6.4 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.ScheduleLookupControllerTest
```

- 예상 결과:
  - `BUILD SUCCESSFUL` 출력

## 7. 결과 확인 방법

- 성공 기준:
  - `date=20260319` 요청 시 날짜 문자열이 `LocalDate`로 변환된 결과가 JSON 응답에 반영됩니다.
  - `requestedDate`가 `2026-03-19`로 직렬화되어 내려옵니다.
  - `date=2026-03-19`처럼 형식이 다른 입력은 `400 Bad Request`를 반환합니다.
- API 확인 방법:
  - `curl` 또는 브라우저로 `GET /mission04/task06/schedules?date=20260319` 호출
  - `curl`로 잘못된 형식 `GET /mission04/task06/schedules?date=2026-03-19` 호출
- 응답 결과 예시:

```http
HTTP/1.1 200
Content-Type: application/json

{
  "requestedDate": "2026-03-19",
  "dayOfWeek": "목요일",
  "agenda": "타입 변환기 설정을 실습하는 날입니다.",
  "message": "query parameter 문자열이 LocalDate로 변환된 뒤 일정 조회에 사용되었습니다."
}
```

```http
HTTP/1.1 400
Content-Type: application/json
```

- 스크린샷 파일명과 저장 위치:
  - 현재 저장소에는 스크린샷 파일을 추가하지 않았습니다.
  - 정상 응답 캡처는 `task06-type-converter-success.png` 파일명으로 `docs/mission-04-spring-mvc/task-06-type-converter/` 경로에 저장하면 됩니다.
  - 잘못된 형식 응답 캡처는 `task06-type-converter-bad-request.png` 파일명으로 `docs/mission-04-spring-mvc/task-06-type-converter/` 경로에 저장하면 됩니다.

## 8. 학습 내용

- 스프링 MVC에서 타입 변환기는 컨트롤러보다 앞단에서 동작합니다. 그래서 컨트롤러는 문자열을 직접 파싱하지 않고도 이미 변환된 `LocalDate`를 바로 사용할 수 있습니다.
- 커스텀 변환기를 등록하면 입력 형식을 프로젝트 규칙에 맞게 통일할 수 있습니다. 이번 예제에서는 기본 ISO 날짜 대신 `yyyyMMdd` 형식을 강제해 변환 규칙을 분명하게 드러냈습니다.
- 잘못된 입력 형식이 들어왔을 때 `400 Bad Request`가 반환된다는 점은 중요합니다. 이는 타입 변환 단계에서 이미 요청이 잘못되었다고 판단했기 때문에, 서비스 로직까지 잘못된 값이 전달되지 않게 막아 줍니다.
- `Converter`와 `WebMvcConfigurer` 조합은 날짜뿐 아니라 숫자 코드, 커스텀 식별자, 열거형 문자열 같은 다양한 사용자 입력 변환에도 그대로 확장할 수 있습니다.
