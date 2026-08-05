package com.workbridge.server.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingConfirmsApplicationAndDatabaseAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/system/ping")
                        .header("X-Trace-Id", "integration-trace-0001"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "integration-trace-0001"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.application").value("WorkBridge"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("UP"))
                .andExpect(jsonPath("$.traceId").value("integration-trace-0001"));
    }

    @Test
    void protectedFutureApiReturnsAuthenticationErrorContract() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }
}
