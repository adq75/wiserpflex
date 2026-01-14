package com.wiseerp;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
// Scan the whole `com.wiseerp` root so test context finds services and components
@ComponentScan(
    basePackages = {"com.wiseerp"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.wiseerp.config.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.wiseerp.health.*")
    }
)
public class TestApplication {
    // Minimal test application to bootstrap Spring Boot test context without JPA/DataSource
}
