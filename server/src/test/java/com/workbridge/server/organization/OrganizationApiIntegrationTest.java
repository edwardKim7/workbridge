package com.workbridge.server.organization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class OrganizationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCreatesUpdatesAndDetectsStaleDepartmentVersion() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        MvcResult createResult = mockMvc.perform(post("/api/v1/departments")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departmentCode": "QUALITY",
                                  "departmentName": "품질관리팀",
                                  "parentDepartmentId": 1,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.departmentCode").value("QUALITY"))
                .andReturn();

        JsonNode created = responseData(createResult);
        long departmentId = created.path("departmentId").asLong();
        long originalVersion = created.path("version").asLong();

        mockMvc.perform(put("/api/v1/departments/{departmentId}", departmentId)
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departmentName": "품질보증팀",
                                  "parentDepartmentId": 1,
                                  "active": true,
                                  "version": %d
                                }
                                """.formatted(originalVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departmentName").value("품질보증팀"))
                .andExpect(jsonPath("$.data.version").value(originalVersion + 1));

        mockMvc.perform(put("/api/v1/departments/{departmentId}", departmentId)
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departmentName": "오래된 수정 요청",
                                  "parentDepartmentId": 1,
                                  "active": true,
                                  "version": %d
                                }
                                """.formatted(originalVersion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
    }

    @Test
    void adminCreatesEmployeeWithBcryptPasswordAndAssignedRole() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(post("/api/v1/employees")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "WB-4001",
                                  "loginId": "quality.user",
                                  "password": "Quality1234!",
                                  "employeeName": "최품질",
                                  "email": "quality.user@workbridge.example",
                                  "departmentId": 1,
                                  "active": true,
                                  "roleIds": [4]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.employeeNo").value("WB-4001"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.roles[0].roleCode").value("VIEWER"));

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM employee WHERE login_id = 'quality.user'",
                String.class
        );
        assertThat(storedHash).isNotEqualTo("Quality1234!");
        assertThat(passwordEncoder.matches("Quality1234!", storedHash)).isTrue();
    }

    @Test
    void employeePasswordOverBcryptUtf8ByteLimitIsRejected() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");
        String multibytePassword = "Aa1!" + "가".repeat(23);

        mockMvc.perform(post("/api/v1/employees")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "employeeNo", "WB-4072",
                                "loginId", "utf8.limit",
                                "password", multibytePassword,
                                "employeeName", "바이트검증",
                                "email", "utf8.limit@workbridge.example",
                                "departmentId", 1,
                                "active", true,
                                "roleIds", java.util.List.of(4)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employee WHERE login_id = 'utf8.limit'",
                Integer.class
        );
        assertThat(count).isZero();
    }

    @Test
    void viewerCanGainReadPermissionButStillCannotWrite() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(put("/api/v1/roles/4/menus")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": 0,
                                  "permissions": [
                                    {"menuId": 1, "canRead": true, "canWrite": false},
                                    {"menuId": 6, "canRead": true, "canWrite": false},
                                    {"menuId": 7, "canRead": true, "canWrite": false}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions[?(@.menuCode == 'EMPLOYEE_ROLE')].canRead").value(org.hamcrest.Matchers.hasItem(true)));

        MockHttpSession viewer = login("viewer", "Viewer1234!");
        mockMvc.perform(get("/api/v1/employees").session(viewer))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/departments")
                        .session(viewer)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departmentCode": "DENIED_WRITE",
                                  "departmentName": "쓰기 차단 검증",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminCannotRemoveProtectedAdministrationPermission() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");

        mockMvc.perform(put("/api/v1/roles/1/menus")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": 0,
                                  "permissions": []
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROTECTED_ADMIN_PERMISSION"));
    }

    @Test
    void deactivatedEmployeeSessionIsRejectedOnNextProtectedRequest() throws Exception {
        MockHttpSession viewer = login("viewer", "Viewer1234!");
        MockHttpSession admin = login("admin", "Admin1234!");
        Long viewerId = jdbcTemplate.queryForObject(
                "SELECT employee_id FROM employee WHERE login_id = 'viewer'",
                Long.class
        );
        Long version = jdbcTemplate.queryForObject(
                "SELECT version FROM employee WHERE employee_id = ?",
                Long.class,
                viewerId
        );

        mockMvc.perform(post("/api/v1/employees/{employeeId}/deactivate", viewerId)
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/v1/auth/me").session(viewer))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void lastActiveAdminRoleCannotBeRemoved() throws Exception {
        MockHttpSession admin = login("admin", "Admin1234!");
        Long version = jdbcTemplate.queryForObject(
                "SELECT version FROM employee WHERE login_id = 'admin'",
                Long.class
        );

        mockMvc.perform(put("/api/v1/employees/1/roles")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[4],\"version\":" + version + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_ACTIVE_ADMIN_REQUIRED"));
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
