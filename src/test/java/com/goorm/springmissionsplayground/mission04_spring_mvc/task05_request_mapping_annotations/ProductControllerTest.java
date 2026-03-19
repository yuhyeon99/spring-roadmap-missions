package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.controller.ProductController;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository.InMemoryProductRepository;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.service.ProductCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        ProductCatalogService service = new ProductCatalogService(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();
    }

    @Test
    @DisplayName("GET /products 는 현재 상품 목록을 반환한다")
    void getProductsReturnsList() throws Exception {
        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("스프링 입문서"))
                .andExpect(jsonPath("$[0].message").value("GET /products 요청으로 조회된 상품입니다."));
    }

    @Test
    @DisplayName("POST /products 는 새 상품을 추가하고 201 Created를 반환한다")
    void postProductsCreatesNewProduct() throws Exception {
        mockMvc.perform(post("/mission04/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "요청 매핑 실습 노트",
                                  "price": 19000,
                                  "category": "문구"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/mission04/task05/products/3")))
                .andExpect(jsonPath("$.name").value("요청 매핑 실습 노트"))
                .andExpect(jsonPath("$.price").value(19000))
                .andExpect(jsonPath("$.category").value("문구"))
                .andExpect(jsonPath("$.message").value("POST /products 요청으로 새 상품이 등록되었습니다."));
    }

    @Test
    @DisplayName("같은 URL 이라도 GET 과 POST 는 서로 다른 메서드로 매핑된다")
    void getAndPostAreMappedSeparatelyOnSameUrl() throws Exception {
        mockMvc.perform(post("/mission04/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "REST API 연습 세트",
                                  "price": 32000,
                                  "category": "학습도구"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].name").value("REST API 연습 세트"));
    }
}
