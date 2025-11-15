package com.visualpathit.account.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Kubernetes probes
 *
 * Provides minimal health check endpoints for:
 * - Liveness probe: /health
 * - Readiness probe: /ready
 * - Version info: /version
 */
@RestController
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private DataSource dataSource;

    @Value("${app.name:facelink}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.buildTime:unknown}")
    private String buildTime;

    /**
     * Liveness probe - Simple check that application is running
     * Returns HTTP 200 if the application process is alive
     * No database or external dependency checks
     *
     * @return {"status": "UP"}
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe - Check if application is ready to serve traffic
     * Validates MySQL database connectivity
     * Returns HTTP 200 if ready, HTTP 503 if not ready
     *
     * @return {"status": "UP/DOWN", "database": "UP/DOWN"}
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();

        // Check MySQL connectivity
        boolean databaseUp = checkDatabaseConnection();

        if (databaseUp) {
            response.put("status", "UP");
            response.put("database", "UP");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "DOWN");
            response.put("database", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Version endpoint - Build information
     * Returns application metadata for deployment tracking
     *
     * @return {"app": "facelink", "version": "1.0.0", "buildTime": "..."}
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> version() {
        Map<String, String> response = new HashMap<>();
        response.put("app", appName);
        response.put("version", appVersion);
        response.put("buildTime", buildTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Check database connection with timeout
     * Uses DataSource connection pool with 2 second timeout
     *
     * @return true if connection successful, false otherwise
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            // Set a short timeout (2 seconds) for the validation query
            if (!connection.isValid(2)) {
                logger.warn("Database connection validation failed");
                return false;
            }
            return true;
        } catch (SQLException e) {
            logger.error("Database connection check failed: {}", e.getMessage());
            return false;
        }
    }
}
