package com.guideforge.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class BackendApplicationTests {

	// Start a MySQL container for tests (Testcontainers will pull & manage the
	// container)
	static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.33")
			.withDatabaseName("testdb")
			.withUsername("root")
			.withPassword("root");

	static {
		MYSQL.start();
	}

	// Dynamically register the container's JDBC properties into Spring's
	// Environment
	@DynamicPropertySource
	static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		// optional: control SQL init
		registry.add("spring.sql.init.mode", () -> "always");
		registry.add("spring.sql.init.platform", () -> "mysql");
	}

	@Test
	void contextLoads() {
		// simple smoke test to ensure ApplicationContext starts with real MySQL
		// container
	}
}