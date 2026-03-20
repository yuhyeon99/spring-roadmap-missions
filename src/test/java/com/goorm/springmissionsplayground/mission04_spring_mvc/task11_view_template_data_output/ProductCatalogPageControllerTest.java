package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output;

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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class ProductCatalogPageControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기본 요청은 전체 제품 목록을 담아 Thymeleaf 페이지를 렌더링한다")
    void showAllProducts() throws Exception {
        mockMvc.perform(get("/mission04/task11/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task11/product-catalog"))
                .andExpect(model().attribute("selectedCategory", is("all")))
                .andExpect(model().attribute("selectedCategoryLabel", is("전체")))
                .andExpect(model().attribute("products", hasSize(5)))
                .andExpect(model().attribute("totalCount", is(5)))
                .andExpect(content().string(containsString("스프링 MVC 핸드북")))
                .andExpect(content().string(containsString("API Gateway 패턴 노트")))
                .andExpect(content().string(containsString("현재 화면 제품 수")));
    }

    @Test
    @DisplayName("category 파라미터를 전달하면 해당 분류 제품만 반복 출력한다")
    void showProductsByCategory() throws Exception {
        mockMvc.perform(get("/mission04/task11/products").param("category", "cloud"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task11/product-catalog"))
                .andExpect(model().attribute("selectedCategory", is("cloud")))
                .andExpect(model().attribute("selectedCategoryLabel", is("Cloud")))
                .andExpect(model().attribute("products", hasSize(2)))
                .andExpect(model().attribute("totalCount", is(2)))
                .andExpect(content().string(containsString("클라우드 배포 체크리스트")))
                .andExpect(content().string(containsString("API Gateway 패턴 노트")))
                .andExpect(content().string(not(containsString("JPA 빠른 시작"))));
    }
}
