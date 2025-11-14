package com.visualpathit.account.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Factory class to create and configure Flyway instance for Spring XML configuration.
 * This is necessary because Flyway 9.x uses a fluent configuration API that's difficult
 * to express in Spring XML.
 */
public class FlywayFactory {

    private static final Logger logger = LoggerFactory.getLogger(FlywayFactory.class);

    private DataSource dataSource;
    private String[] locations = new String[]{"classpath:db/migration"};
    private boolean baselineOnMigrate = true;
    private String baselineVersion = "0";
    private boolean validateOnMigrate = true;
    private String[] schemas = new String[]{"accounts"};

    /**
     * Create and configure a Flyway instance, then execute migrations
     */
    public Flyway createFlyway() {
        logger.info("========================================");
        logger.info("FLYWAY DATABASE MIGRATION - STARTING");
        logger.info("========================================");
        logger.info("Configuration:");
        logger.info("  - Migration locations: {}", String.join(", ", locations));
        logger.info("  - Target schemas: {}", String.join(", ", schemas));
        logger.info("  - Baseline on migrate: {}", baselineOnMigrate);
        logger.info("  - Validate on migrate: {}", validateOnMigrate);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(validateOnMigrate)
                .schemas(schemas)
                .load();

        logger.info("Checking for pending migrations...");
        MigrationInfo[] pendingMigrations = flyway.info().pending();
        if (pendingMigrations.length > 0) {
            logger.info("Found {} pending migration(s) to apply:", pendingMigrations.length);
            for (MigrationInfo info : pendingMigrations) {
                logger.info("  - V{}: {}", info.getVersion(), info.getDescription());
            }
        } else {
            logger.info("No pending migrations found - database is up to date");
        }

        logger.info("Executing Flyway migrations...");
        MigrateResult result = flyway.migrate();

        logger.info("========================================");
        logger.info("FLYWAY MIGRATION COMPLETED");
        logger.info("========================================");
        logger.info("Migration summary:");
        logger.info("  - Migrations executed: {}", result.migrationsExecuted);
        logger.info("  - Target schema version: {}", result.targetSchemaVersion != null ? result.targetSchemaVersion : "N/A");
        logger.info("  - Database: {}", result.database);
        logger.info("========================================");

        return flyway;
    }

    // Setters for Spring XML property injection

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }
}
