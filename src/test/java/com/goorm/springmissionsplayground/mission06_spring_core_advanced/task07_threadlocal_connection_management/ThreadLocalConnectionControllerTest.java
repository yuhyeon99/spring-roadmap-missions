package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionDemoResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionPerformanceResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionStudyService;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ThreadLocalConnectionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ThreadLocalConnectionStudyService threadLocalConnectionStudyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void demonstrateThreadBoundConnection_reusesSingleConnectionWithinSameThread() {
        ThreadLocalConnectionDemoResult result =
                threadLocalConnectionStudyService.demonstrateThreadBoundConnection("plan-blue", "release-engineer");

        assertThat(result.getOpenedConnectionCount()).isEqualTo(1);
        assertThat(result.getRepositoryVisits()).hasSize(2);
        assertThat(result.getRepositoryVisits().stream()
                .map(visit -> visit.getConnectionId())
                .collect(Collectors.toSet())).hasSize(1);
        assertThat(result.getAuditTrail()).extracting("phase")
                .containsExactly("ACQUIRE", "RELEASE");
    }

    @Test
    void measurePerformance_usesFewerConnectionsWithThreadLocal() {
        ThreadLocalConnectionPerformanceResult result =
                threadLocalConnectionStudyService.measurePerformance(3, 20);

        assertThat(result.getDirectConnectionAcquisitions()).isEqualTo(120);
        assertThat(result.getThreadLocalConnectionAcquisitions()).isEqualTo(3);
        assertThat(result.getReuseSavings()).isEqualTo(117);
    }

    @Test
    void demoEndpoint_returnsSharedConnectionTrace() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/plans/plan-blue/demo")
                        .param("operatorId", "release-engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value("plan-blue"))
                .andExpect(jsonPath("$.openedConnectionCount").value(1))
                .andExpect(jsonPath("$.repositoryVisits", hasSize(2)))
                .andExpect(jsonPath("$.repositoryVisits[0].connectionId").isNumber())
                .andExpect(jsonPath("$.repositoryVisits[0].threadName").exists())
                .andExpect(jsonPath("$.repositoryVisits[0].databaseProduct").value("H2"))
                .andExpect(jsonPath("$.auditTrail", hasSize(2)))
                .andExpect(jsonPath("$.auditTrail[0].phase").value("ACQUIRE"))
                .andExpect(jsonPath("$.auditTrail[1].phase").value("RELEASE"));
    }

    @Test
    void performanceEndpoint_returnsConnectionSavings() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/performance")
                        .param("workerCount", "4")
                        .param("iterationsPerWorker", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerCount").value(4))
                .andExpect(jsonPath("$.iterationsPerWorker").value(30))
                .andExpect(jsonPath("$.directConnectionAcquisitions").value(240))
                .andExpect(jsonPath("$.threadLocalConnectionAcquisitions").value(4))
                .andExpect(jsonPath("$.reuseSavings").value(236))
                .andExpect(jsonPath("$.notes", hasSize(3)));
    }

    @Test
    void performanceEndpoint_withInvalidWorkerCount_returns400() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/performance")
                        .param("workerCount", "0")
                        .param("iterationsPerWorker", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("workerCount는 1 이상이어야 합니다."));
    }
}
