package com.workbridge.server.exception;

import com.workbridge.server.audit.TraceIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void returnsConsistentBusinessErrorResponse() throws Exception {
        mockMvc.perform(get("/test/business").header("X-Trace-Id", "error-trace-0001"))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Trace-Id", "error-trace-0001"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("중복 요청입니다."))
                .andExpect(jsonPath("$.traceId").value("error-trace-0001"))
                .andExpect(jsonPath("$.path").value("/test/business"));
    }

    @Test
    void returnsFieldErrorsWithoutExposingSensitiveRejectedValues() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors[*].rejectedValue", hasItems("[MASKED]", "***")));
    }

    @Test
    void returnsServiceUnavailableWhenDatabaseTransactionCannotStart() throws Exception {
        mockMvc.perform(get("/test/database"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("데이터베이스 연결 상태를 확인해 주세요."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void returnsServiceUnavailableWhenDatabaseQueryFails() throws Exception {
        mockMvc.perform(get("/test/data-access"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void returnsMalformedRequestWithoutStackTrace() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @GetMapping("/business")
        String businessError() {
            throw new BusinessException(ErrorCode.CONFLICT, "중복 요청입니다.");
        }

        @GetMapping("/database")
        String databaseError() {
            throw new CannotCreateTransactionException("connection refused");
        }

        @GetMapping("/data-access")
        String dataAccessError() {
            throw new DataAccessResourceFailureException("query connection lost");
        }

        @PostMapping("/validation")
        String validation(@Valid @RequestBody ValidationRequest request) {
            return request.getEmail();
        }
    }

    public static class ValidationRequest {

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
