package com.wiseerp.metadata.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrationServiceTest {
    @Test
    void runsAllMigrations() {
        List<String> run = new ArrayList<>();
        Runnable m1 = () -> run.add("m1");
        Runnable m2 = () -> run.add("m2");
        MigrationService svc = new MigrationService();
        svc.runMigrations(List.of(m1, m2));
        assertThat(run).containsExactly("m1", "m2");
    }
}
