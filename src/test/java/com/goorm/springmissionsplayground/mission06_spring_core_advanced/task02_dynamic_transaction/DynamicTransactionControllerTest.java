package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service.OrderSettlementService;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class DynamicTransactionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderSettlementService orderSettlementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void orderSettlementService_isJdkDynamicProxy() {
        assertThat(Proxy.isProxyClass(orderSettlementService.getClass())).isTrue();
    }

    @Test
    void settle_whenSuccess_thenCommitAndExposeLastTransaction(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/mission06/task02/dynamic-transaction/settlements/order-7001")
                        .param("amount", "42000")
                        .param("operator", "batch-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-7001"))
                .andExpect(jsonPath("$.businessStatus").value("SETTLEMENT_COMPLETED"))
                .andExpect(jsonPath("$.transaction.phase").value("COMMITTED"))
                .andExpect(jsonPath("$.transaction.active").value(false))
                .andExpect(jsonPath("$.transaction.methodName").value("settle"))
                .andExpect(jsonPath("$.transaction.events[0]").value(org.hamcrest.Matchers.startsWith("[TX-BEGIN]")));

        mockMvc.perform(get("/mission06/task02/dynamic-transaction/transactions/last-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("COMMITTED"));

        assertThat(output).contains("[TX-BEGIN]");
        assertThat(output).contains("[TX-STATUS]");
        assertThat(output).contains("[TX-COMMIT]");
        assertThat(output).contains("[BUSINESS] 원장 반영 완료");
    }

    @Test
    void settle_whenFailure_thenRollback(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/mission06/task02/dynamic-transaction/settlements/order-7002/fail")
                        .param("amount", "33000")
                        .param("operator", "batch-admin"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("TX_ROLLBACK"))
                .andExpect(jsonPath("$.transaction.phase").value("ROLLED_BACK"))
                .andExpect(jsonPath("$.transaction.active").value(false))
                .andExpect(jsonPath("$.transaction.methodName").value("settleWithFailure"))
                .andExpect(jsonPath("$.transaction.failureReason").value("외부 원장 반영 실패로 트랜잭션을 롤백합니다."));

        assertThat(output).contains("[TX-ROLLBACK]");
        assertThat(output).contains("외부 원장 반영 실패로 트랜잭션을 롤백합니다.");
    }
}
