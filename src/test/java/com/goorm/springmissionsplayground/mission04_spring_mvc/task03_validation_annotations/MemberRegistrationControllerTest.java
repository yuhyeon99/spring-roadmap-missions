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
