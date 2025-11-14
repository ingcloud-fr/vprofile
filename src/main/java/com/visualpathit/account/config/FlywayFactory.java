package com.visualpathit.account.config;

import org.flywaydb.core.Flyway;
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
        logger.info("Configuring Flyway for database migrations...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(validateOnMigrate)
                .schemas(schemas)
                .load();

        logger.info("Executing Flyway migrations...");
        flyway.migrate();
        logger.info("Flyway migrations completed successfully");

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
