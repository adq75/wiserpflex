package com.wiseerp.metadata.repository;

import com.wiseerp.metadata.model.FieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, UUID> {
	List<FieldDefinition> findByEntityDefinitionId(UUID entityDefinitionId);
}
