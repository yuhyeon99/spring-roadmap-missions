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
