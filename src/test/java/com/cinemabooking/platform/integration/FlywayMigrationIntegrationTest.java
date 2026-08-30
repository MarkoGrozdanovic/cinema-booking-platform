package com.cinemabooking.platform.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class FlywayMigrationIntegrationTest extends PostgreSQLIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayShouldApplyAllMigrations() {
        Integer successfulMigrations =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(2, successfulMigrations);
    }

    @Test
    void flywayShouldCreateApplicationTables() {
        String appUsersTable =
                jdbcTemplate.queryForObject(
                        """
                        SELECT to_regclass(
                            'public.app_users'
                        )
                        """,
                        String.class
                );

        assertNotNull(appUsersTable);
        assertEquals("app_users", appUsersTable);
    }
}
