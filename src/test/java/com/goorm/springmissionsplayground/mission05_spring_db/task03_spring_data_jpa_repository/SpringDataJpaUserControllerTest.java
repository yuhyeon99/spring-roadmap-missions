package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository.SpringDataJpaUserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringDataJpaUserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringDataJpaUserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("Spring Data JPA Repository 기반 사용자 CRUD와 이메일 조회가 동작한다")
    void userCrudFlowWithEmailSearch() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/mission05/task03/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "박리포지토리",
                                  "email": "repository@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("박리포지토리"))
                .andExpect(jsonPath("$.email").value("repository@example.com"))
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        Long userId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/mission05/task03/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("박리포지토리"))
                .andExpect(jsonPath("$.email").value("repository@example.com"));

        mockMvc.perform(get("/mission05/task03/users/search")
                        .param("email", "repository@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("repository@example.com"));

        mockMvc.perform(get("/mission05/task03/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId))
                .andExpect(jsonPath("$[0].name").value("박리포지토리"));

        mockMvc.perform(put("/mission05/task03/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "박리포지토리수정",
                                  "email": "repository-updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("박리포지토리수정"))
                .andExpect(jsonPath("$.email").value("repository-updated@example.com"));

        mockMvc.perform(post("/mission05/task03/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "중복사용자",
                                  "email": "repository-updated@example.com"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/mission05/task03/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission05/task03/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
