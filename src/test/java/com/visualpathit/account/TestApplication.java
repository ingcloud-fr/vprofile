package com.visualpathit.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Test Configuration for Spring Boot Tests
 *
 * This class provides @SpringBootConfiguration for integration tests.
 * It scans the com.visualpathit.account package for components.
 *
 * Note: DataSource is excluded to use H2 in-memory database for tests
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
