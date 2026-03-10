package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.controller.HttpMethodsDemoController;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository.HttpMethodNoteRepository;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service.HttpMethodNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpMethodsDemoControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private HttpMethodNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HttpMethodNoteRepository();
        HttpMethodNoteService service = new HttpMethodNoteService(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(new HttpMethodsDemoController(service)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void postCreatesNoteAndReturns201() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("GET 소개", "GET은 리소스를 조회합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/mission03/task01/notes/")))
                .andExpect(jsonPath("$.title").value("GET 소개"))
                .andExpect(jsonPath("$.content").value("GET은 리소스를 조회합니다."));
    }

    @Test
    void getReturnsListAfterPost() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("POST 소개", "POST는 리소스를 생성합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("POST 소개"));
    }

    @Test
    void putIsIdempotentForSamePayload() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("PUT 소개", "PUT은 전체 리소스를 대체합니다.");

        mockMvc.perform(put("/mission03/task01/notes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("PUT 소개"));

        mockMvc.perform(put("/mission03/task01/notes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteRemovesResource() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("DELETE 소개", "DELETE는 리소스를 제거합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/mission03/task01/notes/{id}", 1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
