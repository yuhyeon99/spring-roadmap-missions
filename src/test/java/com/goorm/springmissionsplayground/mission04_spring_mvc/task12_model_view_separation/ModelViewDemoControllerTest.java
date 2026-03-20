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
