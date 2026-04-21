package com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query;

import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.service.JdbcTemplateBookQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class JdbcTemplateBookQueryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplateBookQueryService jdbcTemplateBookQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jdbcTemplateBookQueryService.resetSampleData();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("JdbcTemplate으로 조회한 데이터를 응답으로 반환하고 콘솔에도 출력한다")
    void queryBooksAndPrintToConsole(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/mission05/task08/books/console-query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("스프링 입문"))
                .andExpect(jsonPath("$[1].author").value("이데이터"))
                .andExpect(jsonPath("$[2].level").value("ADVANCED"))
                .andExpect(jsonPath("$[0].level", startsWith("BEGIN")));

        assertThat(output).contains("=== mission05 task08 JdbcTemplate 조회 결과 시작 ===");
        assertThat(output).contains("id=1, title=스프링 입문, author=김스프링, level=BEGINNER");
        assertThat(output).contains("id=3, title=트랜잭션 실전, author=박트랜잭션, level=ADVANCED");
        assertThat(output).contains("=== mission05 task08 JdbcTemplate 조회 결과 끝 ===");
    }
}
