package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.AopPerformanceComparisonService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.BaselineProjectionService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.OptimizedProjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AopPerformanceOptimizationControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BaselineProjectionService baselineProjectionService;

    @Autowired
    private OptimizedProjectionService optimizedProjectionService;

    @Autowired
    private AopPerformanceComparisonService aopPerformanceComparisonService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void projectionServices_areProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(baselineProjectionService)).isTrue();
        assertThat(AopUtils.isAopProxy(optimizedProjectionService)).isTrue();
    }

    @Test
    void compare_returnsFasterResultForOptimizedAspect() {
        AopPerformanceComparisonResponse response = aopPerformanceComparisonService.compare(120, 480);

        assertThat(response.isResultEquality()).isTrue();
        assertThat(response.getBaselineInvocationCount()).isEqualTo(120);
        assertThat(response.getOptimizedInvocationCount()).isEqualTo(120);
        assertThat(response.getBaselineSnapshotCount()).isEqualTo(120);
        assertThat(response.getOptimizedSnapshotCount()).isLessThan(response.getBaselineSnapshotCount());
        assertThat(response.getOptimizedMetadataCacheMissCount()).isEqualTo(1L);
        assertThat(response.getOptimizedMetadataCacheHitCount()).isEqualTo(119L);
        assertThat(response.getOptimizedElapsedNanos()).isLessThan(response.getBaselineElapsedNanos());
        assertThat(response.getImprovementPercent()).isGreaterThan(15.0);
    }

    @Test
    void compareEndpoint_returnsMeasuredMetrics() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/compare")
                        .param("iterations", "80")
                        .param("payloadSize", "320"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iterations").value(80))
                .andExpect(jsonPath("$.payloadSize").value(320))
                .andExpect(jsonPath("$.baselineSnapshotCount").value(80))
                .andExpect(jsonPath("$.resultEquality").value(true))
                .andExpect(jsonPath("$.optimizationStrategies", hasSize(3)));
    }

    @Test
    void strategiesEndpoint_returnsOptimizationPlan() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategies", hasSize(3)))
                .andExpect(jsonPath("$.strategies[0]").value("포인트컷을 @OptimizedTrace 메서드로 좁혀 비교 대상 메서드만 추적합니다."));
    }

    @Test
    void compareEndpoint_withInvalidIterations_returns400() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/compare")
                        .param("iterations", "0")
                        .param("payloadSize", "320"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("iterations는 1 이상이어야 합니다."));
    }
}
