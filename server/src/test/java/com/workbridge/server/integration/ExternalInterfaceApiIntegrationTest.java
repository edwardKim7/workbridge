package com.workbridge.server.integration;

import com.workbridge.server.dto.integration.ErpSalesResult;
import com.workbridge.server.integration.ErpCallException;
import com.workbridge.server.integration.ErpRestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:workbridge-interface-${random.uuid};MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExternalInterfaceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ErpRestClient erpRestClient;

    @Test
    void approvedOpportunityIsSentAndStoredAsSuccess() throws Exception {
        when(erpRestClient.send(any(), anyString(), any(), anyString()))
                .thenReturn(new ErpRestClient.ErpHttpResult(
                        200,
                        "{\"documentNo\":\"ERP-100001\",\"duplicate\":false,\"message\":\"ok\"}",
                        new ErpSalesResult("ERP-100001", false, "ok")
                ));

        MockHttpSession admin = loginAdmin();
        mockMvc.perform(post("/api/v1/interfaces/erp/2/send")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"simulationMode\":\"NORMAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.idempotencyKey").value("WB:ERP_SALES:OPPORTUNITY:2:v0"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM external_interface_attempt WHERE status='SUCCESS'",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void http500IsStoredAndManualRetryUsesTheSameIdempotencyKey() throws Exception {
        when(erpRestClient.send(any(), anyString(), any(), anyString()))
                .thenThrow(new ErpCallException(
                        "ERP_HTTP_500",
                        "Mock ERP HTTP 500",
                        true,
                        true,
                        500,
                        "{\"code\":\"MOCK_HTTP_500\"}",
                        null
                ));

        MockHttpSession admin = loginAdmin();
        MvcResult failure = mockMvc.perform(post("/api/v1/interfaces/erp/2/send")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"simulationMode\":\"HTTP_500\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TEMPORARY_FAILED"))
                .andExpect(jsonPath("$.data.retryCount").value(1))
                .andReturn();

        Number interfaceIdValue = com.jayway.jsonpath.JsonPath.read(
                failure.getResponse().getContentAsString(),
                "$.data.interfaceId"
        );
        long interfaceId = interfaceIdValue.longValue();
        String keyBefore = jdbcTemplate.queryForObject(
                "SELECT idempotency_key FROM external_interface_log WHERE interface_id=?",
                String.class,
                interfaceId
        );

        when(erpRestClient.send(any(), anyString(), any(), anyString()))
                .thenReturn(new ErpRestClient.ErpHttpResult(
                        200,
                        "{\"documentNo\":\"ERP-100002\",\"duplicate\":true,\"message\":\"existing\"}",
                        new ErpSalesResult("ERP-100002", true, "existing")
                ));

        mockMvc.perform(post("/api/v1/interfaces/{id}/retry", interfaceId)
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"simulationMode\":\"NORMAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.idempotencyKey").value(keyBefore));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM external_interface_attempt WHERE interface_id=?",
                Integer.class,
                interfaceId
        )).isEqualTo(3);
    }

    @Test
    void timeoutIsRecordedWithoutAutomaticRetry() throws Exception {
        when(erpRestClient.send(any(), anyString(), any(), anyString()))
                .thenThrow(new ErpCallException(
                        "ERP_TIMEOUT",
                        "Mock ERP 응답 시간이 초과되었습니다.",
                        true,
                        false,
                        null,
                        null,
                        null
                ));

        MockHttpSession admin = loginAdmin();
        mockMvc.perform(post("/api/v1/interfaces/erp/2/send")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"simulationMode\":\"TIMEOUT_AFTER_COMMIT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TEMPORARY_FAILED"))
                .andExpect(jsonPath("$.data.retryCount").value(0));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM external_interface_attempt",
                Integer.class
        )).isEqualTo(1);
    }

    private MockHttpSession loginAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", "admin")
                        .param("password", "Admin1234!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/dashboard"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
