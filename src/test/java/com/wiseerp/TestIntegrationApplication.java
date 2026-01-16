package com.wiseerp;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {"com.wiseerp.metadata.repository","com.wiseerp.finance.repository"})
@ComponentScan(
    basePackages = {"com.wiseerp"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {TestDataSourceConfig.class, TestRepositoryConfig.class})
    }
)
public class TestIntegrationApplication {
    // Test entrypoint that enables full auto-configuration but excludes test-only configs
}
