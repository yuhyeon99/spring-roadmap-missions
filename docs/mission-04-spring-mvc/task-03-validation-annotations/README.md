# 스프링 MVC: 검증 애노테이션 사용

이 문서는 `mission-04-spring-mvc`의 `task-03-validation-annotations` 수행 결과를 정리한 보고서입니다. 회원 가입 폼에 `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Min`, `@Max`를 적용하고, 검증 실패 시 같은 폼 화면에서 오류를 다시 보여주는 흐름을 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-03-validation-annotations`
- 목표:
  - 회원 가입 폼 DTO에 검증 애노테이션을 선언해 필수값, 문자열 길이, 숫자 범위, 이메일 형식을 검증한다.
  - `@Valid`와 `BindingResult`를 사용해 검증 실패 시 같은 폼 화면을 다시 렌더링하고, 성공 시 결과 화면을 분리해 보여준다.
  - 컨트롤러, 서비스, DTO, 템플릿, 테스트를 함께 남겨 스프링 MVC의 폼 검증 흐름을 재현 가능한 형태로 정리한다.
- 엔드포인트:
  - `GET /mission04/task03/members/new`
  - `POST /mission04/task03/members/new`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/controller/MemberRegistrationController.java` | 회원 가입 폼 조회와 제출 처리, 검증 실패/성공 뷰 분기 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/service/MemberRegistrationValidationService.java` | 학습 트랙 목록, 검증 규칙 안내, 성공 화면용 결과 데이터 생성 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/dto/MemberRegistrationForm.java` | 폼 입력값과 검증 애노테이션 정의 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/domain/StudyTrackOption.java` | 학습 트랙 선택 항목 표현 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/domain/RegisteredMemberProfile.java` | 검증 통과 후 화면에 보여줄 가입 결과 모델 |
| Template | `src/main/resources/templates/mission04/task03/member-registration-form.html` | 회원 가입 폼과 필드 오류 메시지 출력 화면 |
| Template | `src/main/resources/templates/mission04/task03/member-registration-success.html` | 검증 통과 후 결과 요약 화면 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/MemberRegistrationControllerTest.java` | 폼 조회, 검증 실패, 검증 성공 흐름 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `MemberRegistrationForm`에 이름, 이메일, 나이, 비밀번호, 학습 트랙, 자기소개 필드를 두고 각 필드에 맞는 검증 애노테이션을 선언했습니다. 문자열 필수 입력은 `@NotBlank`, 숫자 필수 입력은 `@NotNull`, 길이 제한은 `@Size`, 숫자 범위는 `@Min`과 `@Max`, 이메일 형식은 `@Email`로 검증합니다.
2. `MemberRegistrationController`는 `GET /mission04/task03/members/new`에서 빈 폼 객체를 화면에 전달하고, `POST /mission04/task03/members/new`에서 `@Valid`와 `BindingResult`를 함께 받아 검증 결과에 따라 뷰를 분기합니다.
3. 검증 오류가 있으면 같은 폼 뷰 `mission04/task03/member-registration-form`을 반환합니다. 이때 `BindingResult`에 담긴 필드 오류를 Thymeleaf의 `th:errors`와 `#fields.hasErrors(...)`로 그대로 출력합니다.
4. 검증이 통과하면 `MemberRegistrationValidationService`가 입력값을 정리해 `RegisteredMemberProfile`을 만들고, 성공 뷰 `mission04/task03/member-registration-success`에 가입 결과를 표시합니다.
5. `MemberRegistrationControllerTest`는 MockMvc로 폼 조회, 잘못된 입력값 제출, 정상 입력값 제출을 각각 검증합니다. 뷰 이름, 모델, 필드 오류, 렌더링된 문구를 함께 검사해 MVC 검증 흐름이 실제로 동작하는지 확인합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberRegistrationController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/controller/MemberRegistrationController.java`
- 역할: 회원 가입 폼 조회와 제출 처리, 검증 실패/성공 뷰 분기
- 상세 설명:
- 기본 경로: `/mission04/task03/members`
- HTTP 메서드/세부 경로: `GET /new`, `POST /new`
- `@ModelAttribute` 메서드로 학습 트랙 목록과 검증 규칙 설명을 모든 요청 모델에 공통으로 추가합니다.
- `register(...)`는 `@Valid` 뒤에 바로 `BindingResult`를 받아 검증 실패 여부를 확인하고, 실패 시 같은 폼 뷰를 반환하며 성공 시 결과 객체를 모델에 담아 성공 뷰를 반환합니다.

<details>
<summary><code>MemberRegistrationController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.RegisteredMemberProfile;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.StudyTrackOption;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto.MemberRegistrationForm;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.service.MemberRegistrationValidationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/mission04/task03/members")
public class MemberRegistrationController {

    private static final String FORM_VIEW_NAME = "mission04/task03/member-registration-form";
    private static final String SUCCESS_VIEW_NAME = "mission04/task03/member-registration-success";

    private final MemberRegistrationValidationService memberRegistrationValidationService;

    public MemberRegistrationController(MemberRegistrationValidationService memberRegistrationValidationService) {
        this.memberRegistrationValidationService = memberRegistrationValidationService;
    }

    @ModelAttribute("studyTrackOptions")
    public List<StudyTrackOption> studyTrackOptions() {
        return memberRegistrationValidationService.studyTrackOptions();
    }

    @ModelAttribute("validationRules")
    public List<String> validationRules() {
        return memberRegistrationValidationService.validationRules();
    }

    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("memberRegistrationForm")) {
            model.addAttribute("memberRegistrationForm", new MemberRegistrationForm());
        }
        model.addAttribute("pageTitle", "검증 애노테이션으로 회원 가입 폼 검증");
        model.addAttribute("pageDescription", "폼 입력값을 DTO에 바인딩한 뒤 @NotNull, @Size 같은 검증 애노테이션으로 필수값과 길이 조건을 확인합니다.");
        model.addAttribute("formAction", "/mission04/task03/members/new");
        return FORM_VIEW_NAME;
    }

    @PostMapping("/new")
    public String register(
            @Valid @ModelAttribute("memberRegistrationForm") MemberRegistrationForm memberRegistrationForm,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("pageTitle", "검증 애노테이션으로 회원 가입 폼 검증");
        model.addAttribute("pageDescription", "입력값 검증에 실패하면 같은 폼 뷰를 다시 렌더링하고, 통과하면 성공 화면으로 이동합니다.");
        model.addAttribute("formAction", "/mission04/task03/members/new");

        if (bindingResult.hasErrors()) {
            model.addAttribute("submissionStatus", "입력값을 다시 확인해 주세요.");
            return FORM_VIEW_NAME;
        }

        RegisteredMemberProfile registeredMember = memberRegistrationValidationService.register(memberRegistrationForm);
        model.addAttribute("pageTitle", "회원 가입 검증 통과 결과");
        model.addAttribute("registeredMember", registeredMember);
        return SUCCESS_VIEW_NAME;
    }
}
```

</details>

### 4.2 `MemberRegistrationValidationService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/service/MemberRegistrationValidationService.java`
- 역할: 학습 트랙 목록, 검증 규칙 안내, 성공 화면용 결과 데이터 생성
- 상세 설명:
- 핵심 공개 메서드: `studyTrackOptions()`, `validationRules()`, `register(MemberRegistrationForm form)`
- 트랜잭션은 사용하지 않으며, 데이터 저장 없이 화면 학습에 필요한 선택지와 결과 객체를 메모리에서 생성합니다.
- `register(...)`는 입력값 공백을 정리하고, 선택한 학습 트랙 코드를 화면용 라벨로 변환한 뒤 성공 페이지용 `RegisteredMemberProfile`을 만듭니다.

<details>
<summary><code>MemberRegistrationValidationService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.RegisteredMemberProfile;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain.StudyTrackOption;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto.MemberRegistrationForm;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberRegistrationValidationService {

    private static final List<StudyTrackOption> STUDY_TRACK_OPTIONS = List.of(
            new StudyTrackOption("mvc", "Spring MVC", "컨트롤러, 모델, 뷰 흐름을 먼저 이해하는 학습 경로"),
            new StudyTrackOption("validation", "입력값 검증", "폼 데이터 검증과 에러 메시지 처리 흐름을 집중 학습하는 경로"),
            new StudyTrackOption("data-binding", "데이터 바인딩", "요청 파라미터가 객체로 묶이는 과정을 연습하는 경로")
    );

    private static final Map<String, String> TRACK_LABELS = Map.of(
            "mvc", "Spring MVC",
            "validation", "입력값 검증",
            "data-binding", "데이터 바인딩"
    );

    public List<StudyTrackOption> studyTrackOptions() {
        return STUDY_TRACK_OPTIONS;
    }

    public List<String> validationRules() {
        return List.of(
                "@NotBlank: 이름, 이메일, 비밀번호, 학습 트랙처럼 비어 있으면 안 되는 문자열을 검증합니다.",
                "@Size: 이름 길이, 비밀번호 길이, 자기소개 최대 길이처럼 문자열 길이 조건을 검증합니다.",
                "@NotNull: 나이처럼 값이 반드시 있어야 하는 숫자 필드를 검증합니다.",
                "@Email, @Min, @Max: 형식과 숫자 범위를 함께 제한해 잘못된 입력을 빠르게 걸러냅니다."
        );
    }

    public RegisteredMemberProfile register(MemberRegistrationForm form) {
        String normalizedName = normalize(form.getName());
        String normalizedEmail = normalize(form.getEmail()).toLowerCase();
        String studyTrackLabel = TRACK_LABELS.getOrDefault(form.getStudyTrack(), "알 수 없는 트랙");
        String introduction = StringUtils.hasText(form.getIntroduction())
                ? normalize(form.getIntroduction())
                : "자기소개를 아직 작성하지 않았습니다.";

        return new RegisteredMemberProfile(
                normalizedName,
                normalizedEmail,
                form.getAge(),
                studyTrackLabel,
                introduction,
                normalizedName + "님, 회원 가입 폼 검증을 모두 통과했습니다.",
                studyTrackLabel + " 학습 트랙으로 다음 화면 구현 실습을 이어갈 수 있습니다."
        );
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s{2,}", " ");
    }
}
```

</details>

### 4.3 `MemberRegistrationForm.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/dto/MemberRegistrationForm.java`
- 역할: 폼 입력값과 검증 애노테이션 정의
- 상세 설명:
- 스프링 MVC가 요청 파라미터를 이 객체에 바인딩하고, `@Valid`가 각 필드의 검증 애노테이션을 읽어 검사합니다.
- `name`, `password`, `introduction`에는 문자열 길이 검증을, `age`에는 널 여부와 숫자 범위 검증을, `email`에는 이메일 형식 검증을 함께 적용했습니다.
- 검증 메시지를 필드 옆에 그대로 보여주기 위해 각 제약 조건에 사용자용 메시지를 직접 작성했습니다.

<details>
<summary><code>MemberRegistrationForm.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MemberRegistrationForm {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 60, message = "이메일은 60자 이하여야 합니다.")
    private String email;

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 14, message = "나이는 14세 이상이어야 합니다.")
    @Max(value = 120, message = "나이는 120세 이하여야 합니다.")
    private Integer age;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "학습 트랙은 필수입니다.")
    @Size(max = 30, message = "학습 트랙 값이 너무 깁니다.")
    private String studyTrack;

    @Size(max = 120, message = "자기소개는 120자 이하여야 합니다.")
    private String introduction;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStudyTrack() {
        return studyTrack;
    }

    public void setStudyTrack(String studyTrack) {
        this.studyTrack = studyTrack;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
```

</details>

### 4.4 `StudyTrackOption.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/domain/StudyTrackOption.java`
- 역할: 학습 트랙 선택 항목 표현
- 상세 설명:
- 드롭다운 선택 항목을 `code`, `label`, `description`으로 분리해 표현하는 값 객체입니다.
- 템플릿에서는 `code`를 `option value`로 사용하고, `label`과 `description`은 화면 안내용으로 사용합니다.
- 문자열 배열보다 의미가 분명해서 화면과 서비스 설명을 함께 유지하기 쉽습니다.

<details>
<summary><code>StudyTrackOption.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain;

public class StudyTrackOption {

    private final String code;
    private final String label;
    private final String description;

    public StudyTrackOption(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
```

</details>

### 4.5 `RegisteredMemberProfile.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/domain/RegisteredMemberProfile.java`
- 역할: 검증 통과 후 화면에 보여줄 가입 결과 모델
- 상세 설명:
- 성공 화면에서 사용할 이름, 이메일, 나이, 선택한 학습 트랙, 자기소개, 다음 단계 안내를 묶어 전달합니다.
- 컨트롤러는 이 객체를 모델에 담기만 하고, 화면용 문자열 조합은 서비스가 담당합니다.
- 저장용 엔티티가 아니라 화면 렌더링 목적의 읽기 전용 결과 객체입니다.

<details>
<summary><code>RegisteredMemberProfile.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain;

public class RegisteredMemberProfile {

    private final String name;
    private final String email;
    private final Integer age;
    private final String studyTrackLabel;
    private final String introduction;
    private final String welcomeMessage;
    private final String nextStep;

    public RegisteredMemberProfile(
            String name,
            String email,
            Integer age,
            String studyTrackLabel,
            String introduction,
            String welcomeMessage,
            String nextStep
    ) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.studyTrackLabel = studyTrackLabel;
        this.introduction = introduction;
        this.welcomeMessage = welcomeMessage;
        this.nextStep = nextStep;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public String getStudyTrackLabel() {
        return studyTrackLabel;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getNextStep() {
        return nextStep;
    }
}
```

</details>

### 4.6 `member-registration-form.html`

- 파일 경로: `src/main/resources/templates/mission04/task03/member-registration-form.html`
- 역할: 회원 가입 폼과 필드 오류 메시지 출력 화면
- 상세 설명:
- `th:object="${memberRegistrationForm}"`와 `th:field="*{...}"`를 사용해 폼 필드와 DTO 필드를 연결합니다.
- `#fields.hasAnyErrors()`와 `th:errors="*{field}"`로 전역 요약 문구와 필드별 오류 메시지를 함께 출력합니다.
- 우측 안내 패널에 이번 태스크에서 사용한 검증 규칙과 학습 트랙 설명을 함께 배치해 화면만 봐도 어떤 제약 조건을 실습하는지 알 수 있게 했습니다.

<details>
<summary><code>member-registration-form.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task03 - Validation Annotations</title>
    <style>
        :root {
            --ink: #153243;
            --muted: #5d7280;
            --paper: rgba(255, 255, 255, 0.92);
            --line: rgba(21, 50, 67, 0.14);
            --accent: #1f7a8c;
            --accent-soft: rgba(31, 122, 140, 0.12);
            --danger: #b42318;
            --danger-soft: rgba(180, 35, 24, 0.08);
            --bg-a: #f4efe7;
            --bg-b: #d4e8ed;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Pretendard", "SUIT", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at top left, rgba(31, 122, 140, 0.16), transparent 28%),
                linear-gradient(140deg, var(--bg-a), var(--bg-b));
        }

        main {
            width: min(1120px, 100%);
            margin: 0 auto;
            padding: 36px 20px 56px;
            display: grid;
            gap: 22px;
        }

        .hero,
        .sheet,
        .panel {
            background: var(--paper);
            border: 1px solid var(--line);
            border-radius: 26px;
            box-shadow: 0 20px 48px rgba(21, 50, 67, 0.08);
        }

        .hero,
        .sheet,
        .panel {
            padding: 28px;
        }

        .eyebrow {
            display: inline-flex;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-size: 0.92rem;
            font-weight: 800;
        }

        h1,
        h2,
        p {
            margin: 0;
        }

        h1 {
            margin-top: 16px;
            font-size: clamp(2rem, 4vw, 3.3rem);
            line-height: 1.1;
        }

        .hero p {
            margin-top: 14px;
            color: var(--muted);
            line-height: 1.75;
        }

        .layout {
            display: grid;
            grid-template-columns: 1.25fr 0.75fr;
            gap: 22px;
        }

        form {
            display: grid;
            gap: 18px;
        }

        .field-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 18px;
        }

        .field {
            display: grid;
            gap: 8px;
        }

        .field.full {
            grid-column: 1 / -1;
        }

        label {
            font-weight: 700;
        }

        input,
        select,
        textarea {
            width: 100%;
            padding: 13px 14px;
            border: 1px solid #c6d4db;
            border-radius: 16px;
            background: #fff;
            color: var(--ink);
            font: inherit;
        }

        textarea {
            min-height: 120px;
            resize: vertical;
        }

        input:focus,
        select:focus,
        textarea:focus {
            outline: 2px solid rgba(31, 122, 140, 0.2);
            border-color: var(--accent);
        }

        .hint {
            color: var(--muted);
            font-size: 0.92rem;
            line-height: 1.5;
        }

        .error-summary,
        .field-error {
            color: var(--danger);
        }

        .error-summary {
            padding: 14px 16px;
            border-radius: 18px;
            background: var(--danger-soft);
            border: 1px solid rgba(180, 35, 24, 0.16);
            font-weight: 700;
        }

        .field-error {
            font-size: 0.9rem;
            line-height: 1.4;
        }

        .actions {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }

        button,
        .ghost-link {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 48px;
            padding: 0 18px;
            border-radius: 999px;
            text-decoration: none;
            font-weight: 800;
            font: inherit;
        }

        button {
            border: none;
            background: var(--ink);
            color: #fff;
            cursor: pointer;
        }

        .ghost-link {
            border: 1px solid var(--line);
            color: var(--ink);
            background: #fff;
        }

        .rule-list,
        .track-list {
            display: grid;
            gap: 12px;
            margin-top: 18px;
        }

        .rule-item,
        .track-item {
            padding: 16px;
            border-radius: 18px;
            border: 1px solid var(--line);
            background: #fff;
        }

        .track-item strong,
        .rule-item strong {
            display: block;
            margin-bottom: 6px;
        }

        .inline-code {
            margin-top: 18px;
            padding: 14px 16px;
            border-radius: 18px;
            background: #11212b;
            color: #f8fafc;
            font-family: "JetBrains Mono", "D2Coding", monospace;
            overflow-x: auto;
        }

        @media (max-width: 900px) {
            .layout,
            .field-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<main>
    <section class="hero">
        <span class="eyebrow">Mission04 Task03</span>
        <h1 th:text="${pageTitle}">검증 애노테이션으로 회원 가입 폼 검증</h1>
        <p th:text="${pageDescription}">
            폼 입력값을 DTO에 바인딩한 뒤 @NotNull, @Size 같은 검증 애노테이션으로 필수값과 길이 조건을 확인합니다.
        </p>
        <div class="inline-code">POST /mission04/task03/members/new</div>
    </section>

    <section class="layout">
        <article class="sheet">
            <form th:action="${formAction}" th:object="${memberRegistrationForm}" method="post">
                <div class="error-summary" th:if="${#fields.hasAnyErrors()}" th:text="${submissionStatus}">
                    입력값을 다시 확인해 주세요.
                </div>

                <div class="field-grid">
                    <div class="field">
                        <label for="name">이름</label>
                        <input id="name" type="text" th:field="*{name}" placeholder="예: 김스프링">
                        <span class="hint">`@NotBlank`, `@Size(min=2, max=20)`</span>
                        <span class="field-error" th:if="${#fields.hasErrors('name')}" th:errors="*{name}">이름 오류</span>
                    </div>

                    <div class="field">
                        <label for="email">이메일</label>
                        <input id="email" type="email" th:field="*{email}" placeholder="spring@example.com">
                        <span class="hint">`@NotBlank`, `@Email`, `@Size(max=60)`</span>
                        <span class="field-error" th:if="${#fields.hasErrors('email')}" th:errors="*{email}">이메일 오류</span>
                    </div>

                    <div class="field">
                        <label for="age">나이</label>
                        <input id="age" type="number" th:field="*{age}" placeholder="23">
                        <span class="hint">`@NotNull`, `@Min(14)`, `@Max(120)`</span>
                        <span class="field-error" th:if="${#fields.hasErrors('age')}" th:errors="*{age}">나이 오류</span>
                    </div>

                    <div class="field">
                        <label for="password">비밀번호</label>
                        <input id="password" type="password" th:field="*{password}" placeholder="8자 이상 입력">
                        <span class="hint">`@NotBlank`, `@Size(min=8, max=20)`</span>
                        <span class="field-error" th:if="${#fields.hasErrors('password')}" th:errors="*{password}">비밀번호 오류</span>
                    </div>

                    <div class="field full">
                        <label for="studyTrack">학습 트랙</label>
                        <select id="studyTrack" th:field="*{studyTrack}">
                            <option value="">선택해 주세요</option>
                            <option th:each="track : ${studyTrackOptions}"
                                    th:value="${track.code}"
                                    th:text="${track.label}">
                                Spring MVC
                            </option>
                        </select>
                        <span class="hint">`@NotBlank`로 선택 여부를 검증합니다.</span>
                        <span class="field-error" th:if="${#fields.hasErrors('studyTrack')}" th:errors="*{studyTrack}">학습 트랙 오류</span>
                    </div>

                    <div class="field full">
                        <label for="introduction">자기소개</label>
                        <textarea id="introduction" th:field="*{introduction}" placeholder="선택 입력, 120자 이하"></textarea>
                        <span class="hint">`@Size(max=120)`로 최대 길이를 제한합니다.</span>
                        <span class="field-error" th:if="${#fields.hasErrors('introduction')}" th:errors="*{introduction}">자기소개 오류</span>
                    </div>
                </div>

                <div class="actions">
                    <button type="submit">회원 가입 검증 실행</button>
                    <a class="ghost-link" th:href="@{/mission04/task03/members/new}">폼 초기화</a>
                </div>
            </form>
        </article>

        <aside class="panel">
            <h2>이 폼에서 확인하는 검증 규칙</h2>
            <div class="rule-list">
                <div class="rule-item" th:each="rule : ${validationRules}">
                    <strong th:text="${rule}">@NotBlank: 비어 있지 않은 문자열 검증</strong>
                </div>
            </div>

            <h2 style="margin-top: 24px;">학습 트랙 안내</h2>
            <div class="track-list">
                <div class="track-item" th:each="track : ${studyTrackOptions}">
                    <strong th:text="${track.label}">Spring MVC</strong>
                    <span th:text="${track.description}">설명</span>
                </div>
            </div>
        </aside>
    </section>
</main>
</body>
</html>
```

</details>

### 4.7 `member-registration-success.html`

- 파일 경로: `src/main/resources/templates/mission04/task03/member-registration-success.html`
- 역할: 검증 통과 후 결과 요약 화면
- 상세 설명:
- 검증이 모두 통과했을 때만 렌더링되는 화면으로, 서비스가 만든 `registeredMember` 모델 값을 그대로 출력합니다.
- 가입 결과를 단순 문자열이 아니라 이름, 이메일, 나이, 학습 트랙, 자기소개, 다음 단계로 나눠 보여줘 폼 처리 이후 화면 구성을 확인할 수 있습니다.
- 다시 폼으로 돌아가는 링크를 함께 둬서 검증 실패/성공 흐름을 반복 실습하기 쉽게 했습니다.

<details>
<summary><code>member-registration-success.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mission04 Task03 - Validation Success</title>
    <style>
        :root {
            --ink: #12263a;
            --muted: #5d7280;
            --accent: #2f855a;
            --accent-soft: rgba(47, 133, 90, 0.12);
            --paper: rgba(255, 255, 255, 0.94);
            --line: rgba(18, 38, 58, 0.12);
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
            font-family: "Pretendard", "SUIT", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at top right, rgba(47, 133, 90, 0.18), transparent 28%),
                linear-gradient(145deg, #eef6ed, #d7e9ea);
        }

        .card {
            width: min(760px, 100%);
            padding: 34px;
            border-radius: 30px;
            border: 1px solid var(--line);
            background: var(--paper);
            box-shadow: 0 24px 60px rgba(18, 38, 58, 0.12);
        }

        .badge {
            display: inline-flex;
            padding: 8px 14px;
            border-radius: 999px;
            background: var(--accent-soft);
            color: var(--accent);
            font-weight: 800;
        }

        h1,
        h2,
        p,
        dl {
            margin: 0;
        }

        h1 {
            margin-top: 16px;
            font-size: clamp(2rem, 5vw, 3.4rem);
            line-height: 1.1;
        }

        p {
            margin-top: 14px;
            color: var(--muted);
            line-height: 1.75;
        }

        dl {
            margin-top: 26px;
            display: grid;
            grid-template-columns: 140px 1fr;
            gap: 14px 18px;
            padding: 22px;
            border-radius: 22px;
            background: #fff;
            border: 1px solid var(--line);
        }

        dt {
            font-weight: 800;
            color: var(--ink);
        }

        dd {
            color: var(--muted);
        }

        .next-step {
            margin-top: 24px;
            padding: 18px 20px;
            border-radius: 20px;
            background: #f7fbf8;
            border: 1px solid var(--line);
        }

        a {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-top: 24px;
            padding: 12px 18px;
            border-radius: 999px;
            background: var(--ink);
            color: #fff;
            text-decoration: none;
            font-weight: 800;
        }
    </style>
</head>
<body>
<section class="card">
    <span class="badge">Mission04 Task03</span>
    <h1 th:text="${pageTitle}">회원 가입 검증 통과 결과</h1>
    <p th:text="${registeredMember.welcomeMessage}">
        김스프링님, 회원 가입 폼 검증을 모두 통과했습니다.
    </p>

    <dl>
        <dt>이름</dt>
        <dd th:text="${registeredMember.name}">김스프링</dd>

        <dt>이메일</dt>
        <dd th:text="${registeredMember.email}">spring@example.com</dd>

        <dt>나이</dt>
        <dd th:text="${registeredMember.age}">23</dd>

        <dt>학습 트랙</dt>
        <dd th:text="${registeredMember.studyTrackLabel}">입력값 검증</dd>

        <dt>자기소개</dt>
        <dd th:text="${registeredMember.introduction}">자기소개를 아직 작성하지 않았습니다.</dd>
    </dl>

    <div class="next-step">
        <h2>다음 단계</h2>
        <p th:text="${registeredMember.nextStep}">
            입력값 검증 학습 트랙으로 다음 화면 구현 실습을 이어갈 수 있습니다.
        </p>
    </div>

    <a th:href="@{/mission04/task03/members/new}">다시 입력해 보기</a>
</section>
</body>
</html>
```

</details>

### 4.8 `MemberRegistrationControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task03_validation_annotations/MemberRegistrationControllerTest.java`
- 역할: 폼 조회, 검증 실패, 검증 성공 흐름 검증
- 상세 설명:
- 검증 시나리오 1: 폼 화면이 빈 DTO와 학습 트랙 목록을 모델에 담아 렌더링되는지 확인합니다.
- 검증 시나리오 2: 잘못된 입력값 제출 시 같은 폼 뷰가 반환되고, 필드 오류가 모델과 화면에 모두 반영되는지 확인합니다.
- 검증 시나리오 3: 유효한 입력값 제출 시 성공 뷰가 렌더링되고, 성공 메시지와 가입 결과 요약이 화면에 표시되는지 확인합니다.

<details>
<summary><code>MemberRegistrationControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class MemberRegistrationControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("회원 가입 폼 페이지는 빈 폼 객체와 트랙 목록을 함께 렌더링한다")
    void showRegistrationForm() throws Exception {
        mockMvc.perform(get("/mission04/task03/members/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task03/member-registration-form"))
                .andExpect(model().attributeExists("memberRegistrationForm"))
                .andExpect(model().attribute("studyTrackOptions", hasSize(3)))
                .andExpect(model().attribute("pageTitle", is("검증 애노테이션으로 회원 가입 폼 검증")))
                .andExpect(content().string(containsString("회원 가입 검증 실행")));
    }

    @Test
    @DisplayName("잘못된 입력값을 제출하면 같은 폼 화면에서 필드 오류를 다시 보여준다")
    void registerWithInvalidInput() throws Exception {
        mockMvc.perform(post("/mission04/task03/members/new")
                        .param("name", "")
                        .param("email", "wrong-email")
                        .param("age", "")
                        .param("password", "1234")
                        .param("studyTrack", "")
                        .param("introduction", "a".repeat(121)))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task03/member-registration-form"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberRegistrationForm",
                        "name",
                        "email",
                        "age",
                        "password",
                        "studyTrack",
                        "introduction"
                ))
                .andExpect(content().string(containsString("입력값을 다시 확인해 주세요.")))
                .andExpect(content().string(containsString("이름은 필수입니다.")))
                .andExpect(content().string(containsString("올바른 이메일 형식이어야 합니다.")))
                .andExpect(content().string(containsString("나이는 필수입니다.")));
    }

    @Test
    @DisplayName("유효한 입력값을 제출하면 성공 화면에 가입 결과를 보여준다")
    void registerWithValidInput() throws Exception {
        mockMvc.perform(post("/mission04/task03/members/new")
                        .param("name", "김스프링")
                        .param("email", "spring@example.com")
                        .param("age", "23")
                        .param("password", "spring1234")
                        .param("studyTrack", "validation")
                        .param("introduction", "검증 애노테이션으로 입력값 흐름을 연습하고 있습니다."))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task03/member-registration-success"))
                .andExpect(model().attributeExists("registeredMember"))
                .andExpect(content().string(containsString("회원 가입 검증 통과 결과")))
                .andExpect(content().string(containsString("김스프링님, 회원 가입 폼 검증을 모두 통과했습니다.")))
                .andExpect(content().string(containsString("입력값 검증")));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `@Valid`와 `BindingResult`

- 핵심: `@Valid`는 바인딩된 객체에 선언된 Bean Validation 제약 조건을 검사하고, `BindingResult`는 그 결과를 컨트롤러에서 바로 읽을 수 있게 해 줍니다.
- 왜 쓰는가: 검증 실패를 예외로 바로 끊어 버리지 않고, 같은 폼 화면으로 돌아가 사용자에게 어떤 입력이 잘못됐는지 자연스럽게 안내할 수 있습니다.
- 참고 링크:
  - Spring Framework Validation, Data Binding, and Type Conversion: https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html
  - Spring MVC Form Validation: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html

### 5.2 `@NotBlank`, `@NotNull`, `@Size`

- 핵심: `@NotBlank`는 공백만 있는 문자열도 막고, `@NotNull`은 값 자체가 없는 경우를 막고, `@Size`는 문자열이나 컬렉션 길이를 제한합니다.
- 왜 쓰는가: 필드 성격에 맞는 제약을 선언형으로 붙여 두면 컨트롤러 안에서 `if` 문으로 일일이 검사하지 않아도 되고, 검증 규칙이 DTO에 모여 유지보수가 쉬워집니다.
- 참고 링크:
  - Jakarta Bean Validation Specification: https://jakarta.ee/specifications/bean-validation/
  - Hibernate Validator Reference Guide: https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/

### 5.3 Thymeleaf의 `th:field`와 `th:errors`

- 핵심: `th:field`는 폼 필드와 모델 객체 필드를 연결하고, `th:errors`는 해당 필드에 연결된 검증 메시지를 화면에 출력합니다.
- 왜 쓰는가: 폼 재출력 시 사용자가 방금 입력한 값과 오류 메시지를 함께 유지할 수 있어서 서버 렌더링 기반 폼 UX를 단순하게 만들 수 있습니다.
- 참고 링크:
  - Thymeleaf + Spring 공식 튜토리얼: https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html
  - Spring Framework Thymeleaf 안내: https://docs.spring.io/spring-framework/reference/web/webmvc-view/mvc-thymeleaf.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 브라우저 확인

- 폼 화면:
  - `http://localhost:8080/mission04/task03/members/new`
- 잘못된 값으로 제출:
  - 이름을 비우거나, 이메일을 `wrong-email`처럼 입력하거나, 나이를 비우고 제출
- 정상 값으로 제출:
  - 이름 `김스프링`, 이메일 `spring@example.com`, 나이 `23`, 비밀번호 `spring1234`, 학습 트랙 `입력값 검증`

### 6.3 curl로 검증 흐름 확인

```bash
curl -i http://localhost:8080/mission04/task03/members/new

curl -i -X POST http://localhost:8080/mission04/task03/members/new \
  -d "name=" \
  -d "email=wrong-email" \
  -d "age=" \
  -d "password=1234" \
  -d "studyTrack=" \
  -d "introduction=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

curl -i -X POST http://localhost:8080/mission04/task03/members/new \
  -d "name=김스프링" \
  -d "email=spring@example.com" \
  -d "age=23" \
  -d "password=spring1234" \
  -d "studyTrack=validation" \
  --data-urlencode "introduction=검증 애노테이션으로 입력값 흐름을 연습하고 있습니다."
```

- 예상 결과:
  - 첫 번째 요청은 폼 HTML을 반환합니다.
  - 두 번째 요청은 같은 폼 HTML을 다시 반환하며 오류 메시지가 포함됩니다.
  - 세 번째 요청은 성공 화면 HTML을 반환하며 가입 결과가 포함됩니다.

### 6.4 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.MemberRegistrationControllerTest
```

- 예상 결과:
  - 테스트 실행 후 `BUILD SUCCESSFUL`이 출력됩니다.

## 7. 결과 확인 방법

- 성공 기준:
  - `GET /mission04/task03/members/new`에서 회원 가입 폼과 검증 규칙 안내가 함께 보입니다.
  - 잘못된 입력값 제출 시 같은 폼 화면에서 `이름은 필수입니다.`, `올바른 이메일 형식이어야 합니다.`, `나이는 필수입니다.` 같은 오류 메시지가 표시됩니다.
  - 정상 입력값 제출 시 `회원 가입 검증 통과 결과` 화면이 열리고 이름, 이메일, 나이, 학습 트랙, 자기소개가 요약됩니다.
- API/화면 확인 방법:
  - 브라우저로 폼 화면을 열고 잘못된 값과 정상 값을 각각 제출합니다.
  - 터미널에서는 `curl` 응답 HTML에 오류 문구 또는 성공 문구가 포함되는지 확인합니다.
- 스크린샷 파일명과 저장 위치:
  - 현재 저장소에는 스크린샷 파일을 추가하지 않았습니다.
  - 폼 화면 캡처는 `member-registration-form.png` 파일명으로 `docs/mission-04-spring-mvc/task-03-validation-annotations/member-registration-form.png` 경로에 저장하면 됩니다.
  - 성공 화면 캡처는 `member-registration-success.png` 파일명으로 `docs/mission-04-spring-mvc/task-03-validation-annotations/member-registration-success.png` 경로에 저장하면 됩니다.

## 8. 학습 내용

- 이번 태스크에서 중요한 점은 검증 규칙이 컨트롤러가 아니라 DTO에 모여 있다는 것입니다. 이렇게 하면 폼이 늘어나도 검증 조건을 한 파일에서 확인할 수 있고, 같은 DTO를 다른 컨트롤러에서 재사용할 때도 규칙이 유지됩니다.
- `@Valid`는 바인딩이 끝난 뒤 자동으로 검증을 수행하고, `BindingResult`는 그 결과를 바로 옆 파라미터로 받습니다. 이 둘을 함께 쓰면 검증 실패를 예외 처리로 보내지 않고, 사용자가 입력한 값을 유지한 상태로 같은 폼을 다시 그릴 수 있습니다.
- 문자열 필드는 `@NotBlank`, 숫자 필드는 `@NotNull`처럼 타입과 입력 특성에 맞는 제약을 선택해야 합니다. 예를 들어 나이는 공백 문자열 검사보다 값 존재 여부와 숫자 범위가 더 중요하므로 `Integer` + `@NotNull` + `@Min/@Max` 조합이 자연스럽습니다.
- Thymeleaf의 `th:field`와 `th:errors`는 서버 렌더링 폼에서 매우 실용적입니다. 사용자가 잘못 입력해도 같은 화면에 값과 오류를 함께 다시 보여줄 수 있어서, 스프링 MVC가 폼 처리와 화면 재출력을 어떻게 연결하는지 이해하기 좋습니다.
