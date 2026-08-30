package com.cinemabooking.platform.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_CLASS
)
public abstract class PostgreSQLIntegrationTest {

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRESQL_CONTAINER =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("cinema_booking_test")
                    .withUsername("test")
                    .withPassword("test");
}