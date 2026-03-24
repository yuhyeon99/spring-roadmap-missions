package com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud;

import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.repository.JpaCrudMemberRepository;
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
class JpaCrudMemberControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JpaCrudMemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("회원 CRUD API를 순서대로 호출하면 생성, 조회, 수정, 삭제가 모두 동작한다")
    void memberCrudFlow() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/mission05/task01/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "김스프링",
                                  "email": "springkim@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("김스프링"))
                .andExpect(jsonPath("$.email").value("springkim@example.com"))
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        Long memberId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/mission05/task01/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.name").value("김스프링"))
                .andExpect(jsonPath("$.email").value("springkim@example.com"));

        mockMvc.perform(get("/mission05/task01/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(memberId))
                .andExpect(jsonPath("$[0].name").value("김스프링"));

        mockMvc.perform(put("/mission05/task01/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "김스프링수정",
                                  "email": "updated.springkim@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.name").value("김스프링수정"))
                .andExpect(jsonPath("$.email").value("updated.springkim@example.com"));

        mockMvc.perform(delete("/mission05/task01/members/{id}", memberId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission05/task01/members/{id}", memberId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/mission05/task01/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
