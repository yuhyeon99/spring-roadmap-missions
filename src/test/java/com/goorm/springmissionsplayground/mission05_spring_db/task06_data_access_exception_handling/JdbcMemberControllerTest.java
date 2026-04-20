package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.repository.JdbcMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JdbcMemberControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcMemberRepository jdbcMemberRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jdbcMemberRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("정상 요청이면 JDBC로 회원을 저장하고 조회할 수 있다")
    void createAndReadMember() throws Exception {
        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "김지디비",
                                  "email": "jdbc@example.com",
                                  "grade": "BASIC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/mission05/task06/members/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("김지디비"))
                .andExpect(jsonPath("$.email").value("jdbc@example.com"))
                .andExpect(jsonPath("$.grade").value("BASIC"));

        mockMvc.perform(get("/mission05/task06/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("jdbc@example.com"));
    }

    @Test
    @DisplayName("중복 이메일로 저장하면 SQLException을 잡아 사용자 친화 메시지를 반환한다")
    void duplicateEmailReturnsFriendlyMessage() throws Exception {
        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "첫번째회원",
                                  "email": "duplicate@example.com",
                                  "grade": "BASIC"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "두번째회원",
                                  "email": "duplicate@example.com",
                                  "grade": "ADVANCED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다. 다른 이메일을 입력해주세요."))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/mission05/task06/members"))
                .andExpect(jsonPath("$.sqlState").value("23505"));
    }

    @Test
    @DisplayName("잘못된 SQL을 실행하면 일반 JDBC 오류 메시지를 반환한다")
    void brokenSqlReturnsGenericMessage() throws Exception {
        mockMvc.perform(get("/mission05/task06/members/demo/sql-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("JDBC_PROCESSING_ERROR"))
                .andExpect(jsonPath("$.message").value("잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/mission05/task06/members/demo/sql-error"))
                .andExpect(jsonPath("$.sqlState").isNotEmpty());
    }
}
