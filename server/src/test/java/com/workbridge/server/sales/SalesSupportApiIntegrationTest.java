package com.workbridge.server.sales;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SalesSupportApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCreatesCustomerAndSearchesWithNormalizedBusinessNumber() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        MvcResult createResult = mockMvc.perform(post("/api/v1/customers")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessRegistrationNo": "555-66-77777",
                                  "customerName": "B2B 통합 테스트 고객",
                                  "representativeName": "김대표",
                                  "contactName": "이담당",
                                  "contactEmail": "owner@test.example",
                                  "establishedDate": "2020-01-15",
                                  "assignedSalesId": 2,
                                  "status": "ACTIVE",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.businessRegistrationNo").value("5556677777"))
                .andExpect(jsonPath("$.data.formattedBusinessRegistrationNo").value("555-66-77777"))
                .andExpect(jsonPath("$.data.assignedSalesId").value(2))
                .andReturn();

        long customerId = responseData(createResult).path("customerId").asLong();
        assertThat(customerId).isPositive();

        mockMvc.perform(get("/api/v1/customers")
                        .session(admin)
                        .param("businessRegistrationNo", "555-66")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].customerId").value(customerId));

        mockMvc.perform(get("/api/v1/customers/options")
                        .session(admin)
                        .param("keyword", "B2B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerId").value(customerId));

        mockMvc.perform(get("/api/v1/customers")
                        .session(admin)
                        .param("businessRegistrationNo", "ABC000-00-00001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BUSINESS_REGISTRATION_NO"));
    }

    @Test
    void duplicateBusinessRegistrationNumberIsRejectedAfterNormalization() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(post("/api/v1/customers")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessRegistrationNo": "000-00-00001",
                                  "customerName": "중복 고객",
                                  "assignedSalesId": 2,
                                  "status": "PROSPECT",
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BUSINESS_REGISTRATION_NO"));
    }

    @Test
    void salesCannotAssignNewCustomerToAnotherEmployee() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");

        mockMvc.perform(post("/api/v1/customers")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessRegistrationNo": "888-10-10000",
                                  "customerName": "소유권 차단 고객",
                                  "assignedSalesId": 1,
                                  "status": "PROSPECT",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void viewerCanReadCustomersButCannotCreateThem() throws Exception {
        MockHttpSession viewer = login("viewer", "Viewer1234!");

        mockMvc.perform(get("/api/v1/customers").session(viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3));

        mockMvc.perform(post("/api/v1/customers")
                        .session(viewer)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessRegistrationNo": "777-10-10000",
                                  "customerName": "조회자 등록 차단",
                                  "assignedSalesId": 2,
                                  "status": "PROSPECT",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void opportunityRejectsInvalidProbabilityAndStaleUpdate() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");

        mockMvc.perform(post("/api/v1/opportunities")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityCreateJson("WON", 80)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_OPPORTUNITY_PROBABILITY"));

        MvcResult createResult = mockMvc.perform(post("/api/v1/opportunities")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityCreateJson("PROPOSAL", 50)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        JsonNode created = responseData(createResult);
        long opportunityId = created.path("opportunityId").asLong();
        long version = created.path("version").asLong();

        mockMvc.perform(put("/api/v1/opportunities/{opportunityId}", opportunityId)
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityUpdateJson("NEGOTIATION", 70, version)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").value("NEGOTIATION"))
                .andExpect(jsonPath("$.data.version").value(version + 1));

        mockMvc.perform(put("/api/v1/opportunities/{opportunityId}", opportunityId)
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityUpdateJson("PROPOSAL", 50, version)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
    }

    @Test
    void closedOpportunityCannotReturnToOpenStage() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");

        mockMvc.perform(put("/api/v1/opportunities/4")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityUpdateJson("LEAD", 10, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_OPPORTUNITY_TRANSITION"));
    }

    @Test
    void activityRejectsOpportunityOwnedByAnotherCustomer() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");

        mockMvc.perform(post("/api/v1/activities")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 2,
                                  "opportunityId": 1,
                                  "employeeId": 2,
                                  "activityType": "MEETING",
                                  "subject": "고객-기회 불일치 검증",
                                  "activityAt": "2026-08-05T10:30:00",
                                  "content": "다른 고객의 기회를 연결하면 차단되어야 합니다."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ACTIVITY_TARGET_MISMATCH"));
    }

    @Test
    void salesCreatesActivityAndDashboardUsesOwnScope() throws Exception {
        MockHttpSession sales = login("sales", "Sales1234!");

        mockMvc.perform(post("/api/v1/activities")
                        .session(sales)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "opportunityId": 1,
                                  "employeeId": 2,
                                  "activityType": "VISIT",
                                  "subject": "영업 통합 테스트 방문",
                                  "activityAt": "2026-08-05T14:00:00",
                                  "location": "테스트 회의실",
                                  "content": "고객과 영업기회 연결을 검증합니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.opportunityId").value(1));

        mockMvc.perform(get("/api/v1/activities")
                        .session(sales)
                        .param("keyword", "영업 통합 테스트")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].activityType").value("VISIT"));

        mockMvc.perform(get("/api/v1/sales/dashboard").session(sales))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("MY"))
                .andExpect(jsonPath("$.data.totalCustomers").value(3))
                .andExpect(jsonPath("$.data.openOpportunities").value(3));
    }


    @Test
    void approverUsesMinimalCustomerOptionsWithoutFullCustomerPermission() throws Exception {
        MockHttpSession approver = login("approver", "Approve1234!");

        mockMvc.perform(get("/api/v1/customers/1").session(approver))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/customers/options")
                        .session(approver)
                        .param("keyword", "WorkBridge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerId").value(1))
                .andExpect(jsonPath("$.data[0].customerName").value("WorkBridge 가상고객 A"))
                .andExpect(jsonPath("$.data[0].contactName").doesNotExist())
                .andExpect(jsonPath("$.data[0].contactPhone").doesNotExist())
                .andExpect(jsonPath("$.data[0].contactEmail").doesNotExist());

        mockMvc.perform(get("/api/v1/opportunities/1").session(approver))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerAssignedSalesId").value(2))
                .andExpect(jsonPath("$.data.customerStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.customerActive").value(true));
    }

    @Test
    void inactiveCustomerMakesExistingSalesRecordsReadOnly() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(put("/api/v1/customers/1")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessRegistrationNo": "000-00-00001",
                                  "customerName": "WorkBridge 가상고객 A",
                                  "representativeName": "가상대표 A",
                                  "contactName": "샘플담당 A",
                                  "contactPhone": "02-0000-1001",
                                  "contactEmail": "alpha@workbridge.example",
                                  "establishedDate": "2012-03-15",
                                  "assignedSalesId": 2,
                                  "status": "INACTIVE",
                                  "active": false,
                                  "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(put("/api/v1/opportunities/1")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityUpdateJson("PROPOSAL", 50, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_INACTIVE"));

        mockMvc.perform(put("/api/v1/activities/1")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityId": 1,
                                  "employeeId": 2,
                                  "activityType": "VISIT",
                                  "subject": "비활성 고객 활동 수정 차단",
                                  "activityAt": "2026-08-05T15:00:00",
                                  "location": "WorkBridge 가상고객 A 회의실",
                                  "content": "비활성 고객의 기존 이력은 조회만 허용합니다.",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_INACTIVE"));
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

    private String opportunityCreateJson(String stage, int probability) {
        return """
                {
                  "customerId": 1,
                  "opportunityName": "통합 테스트 영업기회",
                  "stage": "%s",
                  "expectedRevenue": 25000000,
                  "expectedCloseDate": "2026-11-30",
                  "probability": %d,
                  "ownerEmployeeId": 2,
                  "description": "단계와 낙관적 잠금 검증"
                }
                """.formatted(stage, probability);
    }

    private String opportunityUpdateJson(String stage, int probability, long version) {
        return """
                {
                  "opportunityName": "수정 테스트 영업기회",
                  "stage": "%s",
                  "expectedRevenue": 28000000,
                  "expectedCloseDate": "2026-12-15",
                  "probability": %d,
                  "ownerEmployeeId": 2,
                  "description": "수정 검증",
                  "version": %d
                }
                """.formatted(stage, probability, version);
    }
}
