package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope.ApplicationStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WebScopeControllerTest {

    @Autowired
    ApplicationStats applicationStats;

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void resetApplicationStats() {
        applicationStats.reset();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("RequestScope는 요청마다 requestId가 달라진다")
    void requestScopeChangesPerRequest() throws Exception {
        String id1 = extractRequestId(mockMvc.perform(get("/mission02/task13/scopes")));
        String id2 = extractRequestId(mockMvc.perform(get("/mission02/task13/scopes")));

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("SessionScope는 동일 세션에서 cart count를 유지한다")
    void sessionScopePersistsWithinSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/mission02/task13/scopes/cart").session(session))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/mission02/task13/scopes/cart").session(session))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission02/task13/scopes").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.session.itemCount").value(2));
    }

    @Test
    @DisplayName("ApplicationScope는 총 호출 수를 누적한다")
    void applicationScopeAccumulates() throws Exception {
        mockMvc.perform(get("/mission02/task13/scopes")).andExpect(status().isOk());
        mockMvc.perform(get("/mission02/task13/scopes")).andExpect(status().isOk());

        mockMvc.perform(get("/mission02/task13/scopes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.application.totalHits").value(3));
    }

    private String extractRequestId(ResultActions action) throws Exception {
        String response = action.andExpect(status().isOk())
            .andExpect(jsonPath("$.request.requestId").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
        int start = response.indexOf("requestId\":\"") + "requestId\":\"".length();
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
