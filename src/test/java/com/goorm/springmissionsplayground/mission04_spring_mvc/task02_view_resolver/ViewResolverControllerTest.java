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
