package com.wiseerp;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@TestConfiguration
@ConditionalOnProperty(name = "test.override.datasource", havingValue = "true", matchIfMissing = false)
public class TestDataSourceConfig {

    @Bean
    @Qualifier("defaultDataSource")
    public DataSource defaultDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:wiseerp_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }
}
