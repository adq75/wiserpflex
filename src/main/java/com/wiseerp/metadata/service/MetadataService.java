package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.model.FieldDefinition;
import com.wiseerp.metadata.model.MetadataChangeLog;
import com.wiseerp.metadata.model.MetadataVersion;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
import com.wiseerp.metadata.repository.MetadataChangeLogRepository;
import com.wiseerp.metadata.repository.MetadataVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class MetadataService {
    private final EntityDefinitionRepository entityRepo;
    private final FieldDefinitionRepository fieldRepo;
    private final MetadataChangeLogRepository changeLogRepo;
    private final MetadataVersionRepository versionRepo;

    public MetadataService(EntityDefinitionRepository entityRepo,
                           FieldDefinitionRepository fieldRepo,
                           MetadataChangeLogRepository changeLogRepo,
                           MetadataVersionRepository versionRepo) {
        this.entityRepo = entityRepo;
        this.fieldRepo = fieldRepo;
        this.changeLogRepo = changeLogRepo;
        this.versionRepo = versionRepo;
    }

    public EntityDefinition createEntityDefinition(EntityDefinition def) {
        if (def.getId() == null) def.setId(UUID.randomUUID());
        return entityRepo.save(def);
    }

    public EntityDefinition updateEntityDefinition(UUID id, EntityDefinition updated) {
        return entityRepo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDisplayName(updated.getDisplayName());
            existing.setCategory(updated.getCategory());
            existing.setSchemaDefinition(updated.getSchemaDefinition());
            existing.setFields(updated.getFields());
            existing.setVersion(updated.getVersion());
            existing.setIsActive(updated.getIsActive());
            return entityRepo.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("EntityDefinition not found: " + id));
    }

    public void deleteEntityDefinition(UUID id) {
        entityRepo.deleteById(id);
    }

    // FieldDefinition operations
    public FieldDefinition createFieldDefinition(FieldDefinition field) {
        if (field.getId() == null) field.setId(UUID.randomUUID());
        return fieldRepo.save(field);
    }

    public java.util.List<FieldDefinition> listFieldsByEntity(UUID entityDefinitionId) {
        return fieldRepo.findByEntityDefinitionId(entityDefinitionId);
    }

    public FieldDefinition updateFieldDefinition(UUID id, FieldDefinition updated) {
        return fieldRepo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDataType(updated.getDataType());
            existing.setIsRequired(updated.getIsRequired());
            existing.setDefaultValue(updated.getDefaultValue());
            existing.setValidationRules(updated.getValidationRules());
            existing.setUiConfig(updated.getUiConfig());
            existing.setOrderIndex(updated.getOrderIndex());
            return fieldRepo.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("FieldDefinition not found: " + id));
    }

    public void deleteFieldDefinition(UUID id) {
        fieldRepo.deleteById(id);
    }

    public Optional<EntityDefinition> getEntityDefinition(UUID id) {
        return entityRepo.findById(id);
    }

    public List<EntityDefinition> listByTenant(UUID tenantId) {
        return entityRepo.findByTenantId(tenantId);
    }

    public List<EntityDefinition> listAllEntities() {
        return entityRepo.findAll();
    }

    public MetadataChangeLog logChange(MetadataChangeLog log) {
        if (log.getId() == null) log.setId(UUID.randomUUID());
        return changeLogRepo.save(log);
    }

    public MetadataVersion recordVersion(MetadataVersion v) {
        if (v.getId() == null) v.setId(UUID.randomUUID());
        return versionRepo.save(v);
    }
}
