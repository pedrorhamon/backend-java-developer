package com.cmanager.app.core.datasource;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Monitors primary datasource health and updates routing accordingly.
 * Checks every 30 seconds — switches to FALLBACK when primary is down,
 * and restores PRIMARY when it recovers.
 */
@Component
public class DataSourceHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(DataSourceHealthChecker.class);
    private static final int VALIDATION_TIMEOUT_SECONDS = 3;

    private final DataSource primaryDataSource;
    private final RoutingDataSource routingDataSource;

    public DataSourceHealthChecker(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                   RoutingDataSource routingDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.routingDataSource = routingDataSource;
    }

    /**
     * Runs once at startup so routing is set correctly before the first request,
     * even when the primary database is unavailable at boot time.
     */
    @PostConstruct
    public void checkPrimaryOnStartup() {
        log.info("[DataSource] Running initial datasource health check...");
        checkPrimary();
    }

    @Scheduled(fixedDelayString = "${app.datasource.health-check-interval-ms:30000}")
    public void checkPrimary() {
        if (isPrimaryAvailable()) {
            routingDataSource.usePrimary();
        } else {
            routingDataSource.useFallback();
        }
    }

    private boolean isPrimaryAvailable() {
        try (Connection conn = primaryDataSource.getConnection()) {
            return conn.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            log.warn("[DataSource] Primary health check failed: {}", e.getMessage());
            return false;
        }
    }
}
