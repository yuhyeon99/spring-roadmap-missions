package com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.controller.SimpleMvcPageController;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page.service.MvcPageContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SimpleMvcPageControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SimpleMvcPageController controller = new SimpleMvcPageController(new MvcPageContentService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void showPage_rendersViewAndModel() throws Exception {
        mockMvc.perform(get("/mission02/task05/mvc").param("name", "  김스프링  "))
                .andExpect(status().isOk())
                .andExpect(view().name("mission02/task05/home"))
                .andExpect(model().attribute("displayName", "김스프링"))
                .andExpect(model().attribute("topic", "Spring MVC"))
                .andExpect(model().attribute("submitted", false))
                .andExpect(model().attributeExists("welcomeMessage", "serverTime", "learningRequest"))
                .andExpect(model().attribute("learningChecklist", hasSize(4)));
    }

    @Test
    void previewPage_bindsFormValuesAndRendersView() throws Exception {
        mockMvc.perform(post("/mission02/task05/mvc/preview")
                        .param("name", "   ")
                        .param("topic", "Model 과 View 분리"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission02/task05/home"))
                .andExpect(model().attribute("displayName", "학습자"))
                .andExpect(model().attribute("topic", "Model 과 View 분리"))
                .andExpect(model().attribute("submitted", true))
                .andExpect(model().attributeExists("welcomeMessage", "serverTime", "learningChecklist"));
    }
}
