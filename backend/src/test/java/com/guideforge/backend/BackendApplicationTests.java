package com.guideforge.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class BackendApplicationTests {

	// Explicitly specify image; adjust tag if you need a specific MySQL version
	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	// Wire container JDBC props into Spring's environment at runtime
	@DynamicPropertySource
	static void mysqlProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		// If you use a custom driverClassName, you can also set it:
		// registry.add("spring.datasource.driver-class-name", () ->
		// "com.mysql.cj.jdbc.Driver");
	}

	@Test
	void contextLoads() {
		// Simple smoke test to start the Spring context with Testcontainers-backed
		// MySQL
	}
}