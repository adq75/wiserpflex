package com.wiseerp.metadata.init;

import com.wiseerp.metadata.hooks.TenantSchemaCreator;
import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TenantStartupInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TenantStartupInitializer.class);

    private final TenantRepository tenantRepository;
    private final TenantSchemaCreator tenantSchemaCreator;
    private final DataSource defaultDataSource;
    private final int dbMaxAttempts;
    private final long dbDelayMs;

    public TenantStartupInitializer(TenantRepository tenantRepository,
                                    TenantSchemaCreator tenantSchemaCreator,
                                    DataSource defaultDataSource,
                                    @Value("${wiseerp.startup.db.max-attempts:10}") int dbMaxAttempts,
                                    @Value("${wiseerp.startup.db.delay-ms:2000}") long dbDelayMs) {
        this.tenantRepository = tenantRepository;
        this.tenantSchemaCreator = tenantSchemaCreator;
        this.defaultDataSource = defaultDataSource;
        this.dbMaxAttempts = dbMaxAttempts;
        this.dbDelayMs = dbDelayMs;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        log.info("Waiting for database to become ready... (maxAttempts={}, delayMs={})", dbMaxAttempts, dbDelayMs);
        waitForDatabaseReady(dbMaxAttempts, dbDelayMs);

        log.info("Initializing tenant schemas from system.tenants...");
        List<TenantEntity> tenants = tenantRepository.findAll();
        for (TenantEntity t : tenants) {
            try {
                if (Boolean.TRUE.equals(t.getIsActive())) {
                    log.info("Ensuring schema for tenant {} ({})", t.getId(), t.getName());
                    tenantSchemaCreator.createSchemaFor(t.getId());
                } else {
                    log.debug("Skipping inactive tenant {} ({})", t.getId(), t.getName());
                }
            } catch (Exception e) {
                log.error("Failed to ensure schema for tenant {}: {}", t.getId(), e.getMessage(), e);
            }
        }
        log.info("Tenant schema initialization complete.");
    }

    private void waitForDatabaseReady(int maxAttempts, long delayMillis) {
        int attempts = 0;
        final long maxDelay = 30_000L; // cap backoff at 30s
        while (attempts < maxAttempts) {
            attempts++;
            try (Connection c = defaultDataSource.getConnection()) {
                if (c != null && !c.isClosed()) {
                    log.info("Database is ready (attempt {}/{})", attempts, maxAttempts);
                    return;
                }
            } catch (SQLException e) {
                log.warn("Database not ready yet (attempt {}/{}): {}", attempts, maxAttempts, e.getMessage());
            }

            // exponential backoff with jitter
            long expBackoff = delayMillis * (1L << (Math.max(0, attempts - 1)));
            if (expBackoff < 0) expBackoff = maxDelay; // overflow guard
            long sleepMillis = Math.min(expBackoff, maxDelay);
            long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, sleepMillis / 4));
            long totalSleep = sleepMillis + jitter;

            log.debug("Waiting {}ms before next DB readiness attempt (attempt {}/{})", totalSleep, attempts, maxAttempts);
            try {
                Thread.sleep(totalSleep);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for database readiness", ie);
            }
        }
        log.error("Database did not become ready after {} attempts", maxAttempts);
        throw new RuntimeException("Database not ready after startup retries");
    }
}
