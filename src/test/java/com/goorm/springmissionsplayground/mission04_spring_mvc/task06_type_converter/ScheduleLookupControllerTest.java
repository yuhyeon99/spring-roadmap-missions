package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ScheduleLookupControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("yyyyMMdd 형식의 문자열은 LocalDate 로 변환되어 일정 조회에 사용된다")
    void convertsStringQueryParameterToLocalDate() throws Exception {
        mockMvc.perform(get("/mission04/task06/schedules").param("date", "20260319"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedDate").value("2026-03-19"))
                .andExpect(jsonPath("$.dayOfWeek").value("목요일"))
                .andExpect(jsonPath("$.agenda").value("타입 변환기 설정을 실습하는 날입니다."));
    }

    @Test
    @DisplayName("지정한 형식이 아닌 날짜 문자열은 400 Bad Request 를 반환한다")
    void returnsBadRequestWhenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/mission04/task06/schedules").param("date", "2026-03-19"))
                .andExpect(status().isBadRequest());
    }
}
