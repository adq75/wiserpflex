package com.wiseerp.metadata.init;

import com.wiseerp.metadata.generator.ApiGeneratorService;
import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "wiseerp.startup.load-metadata-registry", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class MetadataRegistryInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(MetadataRegistryInitializer.class);

    private final EntityDefinitionRepository entityDefinitionRepository;
    private final ApiGeneratorService apiGeneratorService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Loading metadata entity definitions into API generator registry...");
        try {
            List<EntityDefinition> defs = entityDefinitionRepository.findAll();
            for (EntityDefinition d : defs) {
                if (Boolean.TRUE.equals(d.getIsActive())) {
                    try {
                        apiGeneratorService.register(d);
                    } catch (Exception e) {
                        log.warn("Failed to register entity {}: {}", d.getName(), e.getMessage());
                    }
                }
            }
            log.info("Metadata registry initialization complete (loaded {} entries)", defs.size());
        } catch (Exception ex) {
            log.error("Failed to initialize metadata registry: {}", ex.getMessage(), ex);
        }
    }
}
