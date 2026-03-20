package com.cmanager.app.core.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Dynamic datasource routing with automatic fallback.
 * Routes to PRIMARY by default; switches to FALLBACK when PRIMARY is unavailable.
 * Automatically retries PRIMARY after recovery.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(RoutingDataSource.class);

    private final AtomicReference<DataSourceKey> currentKey =
            new AtomicReference<>(DataSourceKey.PRIMARY);

    @Override
    protected Object determineCurrentLookupKey() {
        return currentKey.get();
    }

    public void usePrimary() {
        if (currentKey.compareAndSet(DataSourceKey.FALLBACK, DataSourceKey.PRIMARY)) {
            log.info("[DataSource] Primary recovered — switching back to PRIMARY");
        }
    }

    public void useFallback() {
        if (currentKey.compareAndSet(DataSourceKey.PRIMARY, DataSourceKey.FALLBACK)) {
            log.warn("[DataSource] Primary unavailable — switching to FALLBACK");
        }
    }

    public DataSourceKey getCurrent() {
        return currentKey.get();
    }
}
