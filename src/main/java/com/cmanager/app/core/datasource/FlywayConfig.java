package com.cmanager.app.core.datasource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Runs Flyway migrations on both PRIMARY and FALLBACK datasources at startup.
 * This ensures both databases always have the same schema,
 * so the fallback is ready to receive traffic at any moment.
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String[] locations;

    @Value("${spring.flyway.baseline-on-migrate:false}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.baseline-version:1}")
    private String baselineVersion;

    @Value("${spring.flyway.clean-disabled:true}")
    private boolean cleanDisabled;

    @Primary
    @Bean(name = "flywayInitializer")
    public FlywayMigrationInitializer flywayInitializer(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("fallbackDataSource") DataSource fallbackDataSource) {

        migrateDataSource("PRIMARY", primaryDataSource);
        migrateDataSource("FALLBACK", fallbackDataSource);

        // Returns a no-op initializer — migrations already ran above
        return new FlywayMigrationInitializer(
                Flyway.configure().dataSource(primaryDataSource).load(),
                flyway -> { /* already migrated */ }
        );
    }

    private void migrateDataSource(String name, DataSource dataSource) {
        try {
            log.info("[Flyway] Running migrations on {} datasource...", name);
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations)
                    .baselineOnMigrate(baselineOnMigrate)
                    .baselineVersion(baselineVersion)
                    .cleanDisabled(cleanDisabled)
                    .load()
                    .migrate();
            log.info("[Flyway] Migrations on {} completed successfully.", name);
        } catch (Exception e) {
            // Neither datasource is mandatory at startup — the health checker decides routing.
            // A down primary is tolerable: the app boots on FALLBACK and switches back automatically.
            log.error("[Flyway] Failed to migrate {} datasource — it may be unreachable: {}", name, e.getMessage());
        }
    }
}
