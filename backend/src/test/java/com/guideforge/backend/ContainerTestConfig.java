package com.guideforge.backend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration to spin up a MySQL container for integration tests.
 * Provides dynamic datasource properties to Spring Boot test context.
 */
public class ContainerTestConfig {

    // Initialize MySQL 8.0 container for tests
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("root");

    // Start container when class is loaded
    static {
        MYSQL.start();
    }

    // Dynamically register datasource properties for Spring Boot
    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.sql.init.mode", () -> "always"); // Ensure init scripts always run
        registry.add("spring.sql.init.platform", () -> "mysql"); // Set init platform to MySQL
    }
}
