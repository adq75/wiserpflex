package com.wiseerp.metadata.repository;

import com.wiseerp.metadata.model.MetadataVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetadataVersionRepository extends JpaRepository<MetadataVersion, UUID> {

}
