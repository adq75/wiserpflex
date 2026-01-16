package com.wiseerp.metadata.integration;

import com.wiseerp.TestIntegrationApplication;
import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestIntegrationApplication.class)
@DirtiesContext
public class RestoreAuditPersistenceIntegrationTest {

    @Autowired
    private MetadataChangeLogRepository repo;

    @Test
    public void sanityCheckRepositoryAvailable() {
        List<MetadataChangeLog> all = repo.findAll();
        assertThat(all).isNotNull();
    }
}
