package com.workbridge.server.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loginPageIsPublicAndContainsCsrfToken() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(request().attribute("_csrf", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void validAdminCredentialsCreateSessionAndExposeCurrentUser() throws Exception {
        MockHttpSession session = login("admin", "Admin1234!");

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("admin"))
                .andExpect(jsonPath("$.data.roles[0].roleCode").value("ADMIN"))
                .andExpect(jsonPath("$.data.menus[?(@.menuCode == 'ADMINISTRATION')]").exists());

        Integer failureCount = jdbcTemplate.queryForObject(
                "SELECT login_fail_count FROM employee WHERE login_id = 'admin'",
                Integer.class
        );
        Timestamp lastLoginAt = jdbcTemplate.queryForObject(
                "SELECT last_login_at FROM employee WHERE login_id = 'admin'",
                Timestamp.class
        );
        Integer auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE action = 'LOGIN_SUCCESS' AND actor_id = 1",
                Integer.class
        );

        assertThat(failureCount).isZero();
        assertThat(lastLoginAt).isNotNull();
        assertThat(auditCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    void invalidPasswordIncrementsFailureCountWithoutLoggingPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", "admin")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        Integer failureCount = jdbcTemplate.queryForObject(
                "SELECT login_fail_count FROM employee WHERE login_id = 'admin'",
                Integer.class
        );
        String auditJson = jdbcTemplate.queryForObject(
                "SELECT after_data FROM audit_log WHERE action = 'LOGIN_FAILURE' ORDER BY audit_id DESC LIMIT 1",
                String.class
        );

        assertThat(failureCount).isEqualTo(1);
        assertThat(auditJson).contains("FAILURE").doesNotContain("wrong-password");
    }

    @Test
    void fifthFailureLocksAccountAndCorrectPasswordDoesNotBypassLock() throws Exception {
        for (int attempt = 0; attempt < 5; attempt += 1) {
            mockMvc.perform(post("/login")
                            .with(csrf())
                            .param("loginId", "admin")
                            .param("password", "Wrong1234!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        Integer failureCount = jdbcTemplate.queryForObject(
                "SELECT login_fail_count FROM employee WHERE login_id = 'admin'",
                Integer.class
        );
        Timestamp lockedUntil = jdbcTemplate.queryForObject(
                "SELECT locked_until FROM employee WHERE login_id = 'admin'",
                Timestamp.class
        );
        assertThat(failureCount).isEqualTo(5);
        assertThat(lockedUntil).isNotNull();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", "admin")
                        .param("password", "Admin1234!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void salesSessionCannotCallAdministrationApi() throws Exception {
        MockHttpSession session = login("sales", "Sales1234!");

        mockMvc.perform(get("/api/v1/employees").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/web/admin/employees").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    void loginPostWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("loginId", "admin")
                        .param("password", "Admin1234!"))
                .andExpect(status().isForbidden());
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
}
