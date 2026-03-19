package com.goorm.springmissionsplayground.mission04_spring_mvc.task07_message_source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class MessageSourceDemoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기본 요청은 기본 Locale 인 한국어 메시지를 렌더링한다")
    void rendersKoreanMessagesByDefault() throws Exception {
        mockMvc.perform(get("/mission04/task07/messages"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task07/message-source-demo"))
                .andExpect(model().attribute("localeCode", is("ko")))
                .andExpect(content().string(containsString("메시지 소스를 활용한 다국어 지원")))
                .andExpect(content().string(containsString("현재 화면은 한국어 메시지 파일에서 읽은 결과입니다.")));
    }

    @Test
    @DisplayName("lang=en 파라미터를 전달하면 영어 메시지를 렌더링한다")
    void rendersEnglishMessagesWhenLangParameterIsEnglish() throws Exception {
        mockMvc.perform(get("/mission04/task07/messages")
                        .param("lang", "en")
                        .param("name", "Spring Learner"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task07/message-source-demo"))
                .andExpect(model().attribute("localeCode", is("en")))
                .andExpect(content().string(containsString("Internationalization with MessageSource")))
                .andExpect(content().string(containsString("Hello, Spring Learner.")))
                .andExpect(content().string(containsString("Current locale: en")));
    }
}
