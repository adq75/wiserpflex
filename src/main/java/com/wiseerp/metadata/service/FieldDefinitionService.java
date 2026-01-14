package com.wiseerp.metadata.service;

import com.wiseerp.metadata.model.FieldDefinition;
import com.wiseerp.metadata.repository.FieldDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FieldDefinitionService {
    private final FieldDefinitionRepository fieldRepo;

    public FieldDefinitionService(FieldDefinitionRepository fieldRepo) {
        this.fieldRepo = fieldRepo;
    }

    public FieldDefinition createField(FieldDefinition f) {
        if (f.getId() == null) f.setId(UUID.randomUUID());
        return fieldRepo.save(f);
    }

    public List<FieldDefinition> listFieldsByEntity(UUID entityDefinitionId) {
        return fieldRepo.findByEntityDefinitionId(entityDefinitionId);
    }

    public Optional<FieldDefinition> getField(UUID id) {
        return fieldRepo.findById(id);
    }

    public FieldDefinition updateField(UUID id, FieldDefinition updated) {
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

    public void deleteField(UUID id) {
        fieldRepo.deleteById(id);
    }
}
