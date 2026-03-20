package com.cmanager.app.core.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configures primary and fallback datasources with automatic routing.
 * Enables @Scheduled for periodic health checks.
 */
@Configuration
@EnableScheduling
public class DataSourceConfig {

    // ── Primary (PostgreSQL principal) ──────────────────────────────────────
    @Value("${spring.datasource.url}")
    private String primaryUrl;

    @Value("${spring.datasource.username}")
    private String primaryUsername;

    @Value("${spring.datasource.password}")
    private String primaryPassword;

    // ── Fallback (PostgreSQL reserva ou Oracle) ─────────────────────────────
    @Value("${app.datasource.fallback.url}")
    private String fallbackUrl;

    @Value("${app.datasource.fallback.username}")
    private String fallbackUsername;

    @Value("${app.datasource.fallback.password}")
    private String fallbackPassword;

    @Value("${app.datasource.fallback.driver-class-name:org.postgresql.Driver}")
    private String fallbackDriverClassName;

    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return buildHikari(
                primaryUrl,
                primaryUsername,
                primaryPassword,
                "org.postgresql.Driver",
                "HikariPool-primary"
        );
    }

    @Bean(name = "fallbackDataSource")
    public DataSource fallbackDataSource() {
        return buildHikari(
                fallbackUrl,
                fallbackUsername,
                fallbackPassword,
                fallbackDriverClassName,
                "HikariPool-fallback"
        );
    }

    @Bean
    @Primary
    public RoutingDataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("fallbackDataSource") DataSource fallback) {

        final RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceKey.PRIMARY, primary,
                DataSourceKey.FALLBACK, fallback
        ));
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();
        return routing;
    }

    private DataSource buildHikari(String url, String username, String password,
                                   String driverClassName, String poolName) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setPoolName(poolName);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setInitializationFailTimeout(-1); // don't fail at pool creation — health checker decides routing
        config.setConnectionTimeout(5_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(1_800_000);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }
}
