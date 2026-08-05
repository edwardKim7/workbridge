package com.workbridge.server.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywaySchemaIntegrationTest {

    private static final List<String> REQUIRED_TABLES = List.of(
            "department", "employee", "role", "employee_role", "menu", "role_menu",
            "customer", "sales_opportunity", "sales_activity", "approval_request",
            "approval_history", "external_interface_log", "external_interface_attempt", "migration_batch",
            "migration_staging", "migration_error", "audit_log", "refresh_token",
            "system_error_log"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesFinalSchemaReferenceDataAndSamples() {
        List<String> actualTables = jdbcTemplate.queryForList(
                "SELECT LOWER(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_SCHEMA) = 'public'",
                String.class
        );

        assertThat(actualTables).containsAll(REQUIRED_TABLES);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `role`", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM department", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employee", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales_opportunity", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales_activity", Integer.class)).isEqualTo(4);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM approval_request", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM approval_history", Integer.class)).isGreaterThanOrEqualTo(8);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE LOWER(TABLE_NAME)='migration_staging' "
                        + "AND LOWER(COLUMN_NAME)='business_registration_no'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM menu WHERE menu_code = 'SALES_ACTIVITY'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role_menu rm "
                        + "INNER JOIN menu m ON m.menu_id = rm.menu_id "
                        + "WHERE m.menu_code = 'SALES_ACTIVITY'", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role_menu rm "
                        + "INNER JOIN `role` r ON r.role_id = rm.role_id "
                        + "INNER JOIN menu m ON m.menu_id = rm.menu_id "
                        + "WHERE r.role_code = 'APPROVER' AND m.menu_code = 'SALES_SUPPORT' "
                        + "AND rm.can_read = TRUE", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales_opportunity "
                        + "WHERE stage = 'WON' AND status = 'CLOSED' AND probability = 100",
                Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flyway_schema_history", Integer.class))
                .isGreaterThanOrEqualTo(8);
    }

}
