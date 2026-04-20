package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.repository.HybridStoreProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class HybridPersistenceControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private HybridStoreProductJpaRepository productJpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productJpaRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("JPA로 저장한 상품을 같은 요청 안에서 MyBatis로 다시 조회할 수 있다")
    void createWithJpaAndReadWithMyBatis() throws Exception {
        mockMvc.perform(post("/mission05/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "백엔드 핸드북",
                                  "category": "BOOK",
                                  "price": 28000,
                                  "stockQuantity": 12
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.writeTechnology").value("JPA"))
                .andExpect(jsonPath("$.readTechnology").value("MyBatis"))
                .andExpect(jsonPath("$.savedByJpa.name").value("백엔드 핸드북"))
                .andExpect(jsonPath("$.readByMyBatis.name").value("백엔드 핸드북"))
                .andExpect(jsonPath("$.categoryStockSummaries[0].category").value("BOOK"))
                .andExpect(jsonPath("$.categoryStockSummaries[0].totalStockQuantity").value(12));
    }

    @Test
    @DisplayName("JPA로 재고를 수정한 뒤 MyBatis 집계 결과에서 변경된 수량을 확인할 수 있다")
    void updateWithJpaAndReadSummaryWithMyBatis() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/mission05/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "스프링 머그컵",
                                  "category": "GOODS",
                                  "price": 15000,
                                  "stockQuantity": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        Long productId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(patch("/mission05/task05/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stockQuantity": 9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedByJpa.stockQuantity").value(9))
                .andExpect(jsonPath("$.readByMyBatis.stockQuantity").value(9))
                .andExpect(jsonPath("$.categoryStockSummaries[0].category").value("GOODS"))
                .andExpect(jsonPath("$.categoryStockSummaries[0].totalStockQuantity").value(9));

        mockMvc.perform(get("/mission05/task05/products/{id}/compare", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jpaTechnology").value("JPA"))
                .andExpect(jsonPath("$.myBatisTechnology").value("MyBatis"))
                .andExpect(jsonPath("$.jpaProduct.stockQuantity").value(9))
                .andExpect(jsonPath("$.myBatisProduct.stockQuantity").value(9));
    }

    @Test
    @DisplayName("MyBatis 전용 조회 엔드포인트에서 전체 목록과 카테고리 집계를 확인할 수 있다")
    void listAndSummaryByMyBatis() throws Exception {
        mockMvc.perform(post("/mission05/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "스프링 입문서",
                                  "category": "BOOK",
                                  "price": 22000,
                                  "stockQuantity": 5
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/mission05/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "아키텍처 노트",
                                  "category": "BOOK",
                                  "price": 32000,
                                  "stockQuantity": 3
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mission05/task05/products/mybatis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("스프링 입문서"))
                .andExpect(jsonPath("$[1].name").value("아키텍처 노트"));

        mockMvc.perform(get("/mission05/task05/products/mybatis/category-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("BOOK"))
                .andExpect(jsonPath("$[0].productCount").value(2))
                .andExpect(jsonPath("$[0].totalStockQuantity").value(8));
    }
}
