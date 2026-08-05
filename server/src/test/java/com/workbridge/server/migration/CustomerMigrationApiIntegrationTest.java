package com.workbridge.server.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:workbridge-migration-${random.uuid};MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomerMigrationApiIntegrationTest {

    private static final String HEADER = "business_registration_no,customer_name,representative_name,contact_name,contact_email,contact_phone,established_date,assigned_sales_employee_no\r\n";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void validCsvIsStagedValidatedAndApplied() throws Exception {
        MockHttpSession admin = loginAdmin();
        MockMultipartFile file = csv(
                "customers-valid.csv",
                HEADER + "777-77-77777,신규 가상고객,김대표,이담당,owner@sample.example,02-7777-7777,2020-01-02,WB-1001\r\n"
        );

        mockMvc.perform(multipart("/api/v1/migrations/customers")
                        .file(file)
                        .session(admin)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batch.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.batch.totalCount").value(1))
                .andExpect(jsonPath("$.data.batch.successCount").value(1))
                .andExpect(jsonPath("$.data.batch.failureCount").value(0));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE business_registration_no='7777777777'",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void invalidRowsAreSeparatedWhileValidRowsAreAppliedAtomically() throws Exception {
        MockHttpSession admin = loginAdmin();
        MockMultipartFile file = csv(
                "customers-mixed.csv",
                HEADER
                        + "8888888888,정상 가상고객,김대표,이담당,valid@sample.example,02-8888-8888,2021-02-03,WB-1001\r\n"
                        + "8888888888,,박대표,최담당,bad-email,02-9999-9999,2026/08/05,UNKNOWN\r\n"
        );

        mockMvc.perform(multipart("/api/v1/migrations/customers")
                        .file(file)
                        .session(admin)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batch.status").value("COMPLETED_WITH_ERRORS"))
                .andExpect(jsonPath("$.data.batch.totalCount").value(2))
                .andExpect(jsonPath("$.data.batch.successCount").value(1))
                .andExpect(jsonPath("$.data.batch.failureCount").value(1))
                .andExpect(jsonPath("$.data.errors.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE business_registration_no='8888888888'",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void duplicateCompletedFileAndWrongExtensionAreRejected() throws Exception {
        MockHttpSession admin = loginAdmin();
        String body = HEADER + "9999999999,중복 파일 가상고객,,,,,2022-01-01,WB-1001\r\n";

        mockMvc.perform(multipart("/api/v1/migrations/customers")
                        .file(csv("customers.csv", body))
                        .session(admin)
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/v1/migrations/customers")
                        .file(csv("customers.csv", body))
                        .session(admin)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_MIGRATION_FILE"));

        MockMultipartFile wrong = new MockMultipartFile(
                "file",
                "customers.txt",
                "text/plain",
                body.getBytes(StandardCharsets.UTF_8)
        );
        mockMvc.perform(multipart("/api/v1/migrations/customers")
                        .file(wrong)
                        .session(admin)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MIGRATION_FILE"));
    }

    private MockMultipartFile csv(String name, String body) {
        return new MockMultipartFile(
                "file",
                name,
                "text/csv",
                body.getBytes(StandardCharsets.UTF_8)
        );
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
