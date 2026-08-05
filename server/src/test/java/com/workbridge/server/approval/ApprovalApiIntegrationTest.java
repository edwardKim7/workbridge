package com.workbridge.server.approval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApprovalApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requestApproveAndDuplicateApproveAreHandledAtomically() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");
        long approvalId = createDraft(sales, "중복 승인 차단 테스트");

        mockMvc.perform(post("/api/v1/approvals/{id}/request", approvalId)
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"opinion\":\"승인 요청\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approval.status").value("REQUESTED"))
                .andExpect(jsonPath("$.data.approval.version").value(1));

        MockHttpSession approver = login("approver", "Approve1234!");
        mockMvc.perform(post("/api/v1/approvals/{id}/approve", approvalId)
                        .session(approver)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"opinion\":\"확인 완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approval.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.histories.length()").value(3));

        mockMvc.perform(post("/api/v1/approvals/{id}/approve", approvalId)
                        .session(approver)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"opinion\":\"중복 클릭\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPROVAL_ALREADY_PROCESSED"));
    }

    @Test
    void anotherUserCannotCancelRequesterDraft() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");
        long approvalId = createDraft(sales, "취소 권한 테스트");
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(post("/api/v1/approvals/{id}/cancel", approvalId)
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"opinion\":\"관리자 취소 시도\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("APPROVAL_CANCEL_NOT_ALLOWED"));
    }

    @Test
    void mobileBearerTokenCanApproveWithoutWebCsrf() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");
        long approvalId = createDraft(sales, "모바일 승인 API 테스트");
        mockMvc.perform(post("/api/v1/approvals/{id}/request", approvalId)
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0}"))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "approver",
                                  "password": "Approve1234!",
                                  "deviceInfo": "MockMvc Android"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();
        String accessToken = responseData(loginResult).path("accessToken").asText();
        assertThat(accessToken).isNotBlank();

        mockMvc.perform(post("/api/v1/approvals/{id}/approve", approvalId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"opinion\":\"모바일 승인\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approval.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/approvals/{id}", approvalId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.histories[2].opinion").value("모바일 승인"));
    }

    private long createDraft(MockHttpSession sales, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/approvals")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approvalType": "SALES_EXPENSE",
                                  "businessType": "OPPORTUNITY",
                                  "businessId": 1,
                                  "approverId": 3,
                                  "title": "%s",
                                  "amount": 120000,
                                  "expenseCategory": "출장비",
                                  "description": "통합 테스트 결재"
                                }
                                """.formatted(title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approval.status").value("DRAFT"))
                .andReturn();
        return responseData(result).path("approval").path("approvalId").asLong();
    }

    private MockHttpSession login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", loginId)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/dashboard"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
