package com.wiseerp.tools;

import com.wiseerp.metadata.generator.DynamicEntityGenerator;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.model.TenantEntity;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = "com.wiseerp")
@Profile("!test")
@ConditionalOnProperty(name = "wiseerp.materialize.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class MaterializeFinanceTablesRunner {
    private static final Logger log = LoggerFactory.getLogger(MaterializeFinanceTablesRunner.class);

    private final EntityDefinitionRepository entityDefinitionRepository;
    private final TenantRepository tenantRepository;
    private final DynamicEntityGenerator dynamicEntityGenerator;

    public static void main(String[] args) {
        SpringApplication.run(MaterializeFinanceTablesRunner.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            log.info("Starting finance materialization for tenants...");

            // finance entity names we want to materialize
            Set<String> financeNames = Set.of("Account", "JournalEntry", "JournalLine");

            List<EntityDefinition> defs = entityDefinitionRepository.findAll();
            List<EntityDefinition> financeDefs = defs.stream()
                    .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                    .filter(d -> financeNames.contains(d.getName()))
                    .toList();

            List<TenantEntity> tenants = tenantRepository.findAll();

            if (financeDefs.isEmpty()) {
                log.warn("No active finance entity definitions found to materialize.");
            }

            for (TenantEntity t : tenants) {
                UUID tenantId = t.getId();
                log.info("Materializing finance entities for tenant {} (schema={})", tenantId, t.getSchemaName());
                for (EntityDefinition def : financeDefs) {
                    try {
                        dynamicEntityGenerator.createTable(tenantId, def, false);
                        log.info("Created table for entity {} for tenant {}", def.getName(), tenantId);
                    } catch (Exception ex) {
                        log.error("Failed to create table for entity {} for tenant {}: {}", def.getName(), tenantId, ex.getMessage());
                    }
                }
            }

            log.info("Finance materialization complete. Exiting.");
        } finally {
            // terminate JVM to avoid leaving Spring context running
            System.exit(0);
        }
    }
}
