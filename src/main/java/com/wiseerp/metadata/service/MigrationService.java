package com.wiseerp.metadata.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lightweight migration runner for metadata schema changes.
 * Migrations are represented as Runnable lambdas or method references.
 */
@Service
public class MigrationService {
    public void runMigrations(List<Runnable> migrations) {
        for (Runnable m : migrations) {
            m.run();
        }
    }
}
