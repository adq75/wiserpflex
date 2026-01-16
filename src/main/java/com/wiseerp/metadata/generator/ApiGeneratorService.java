package com.wiseerp.metadata.generator;

import com.wiseerp.metadata.model.EntityDefinition;
import com.wiseerp.metadata.repository.EntityDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.wiseerp.metadata.query.DynamicQueryBuilder;
import com.wiseerp.metadata.query.DynamicQueryBuilder.Query;
import com.wiseerp.metadata.query.DynamicQueryExecutor;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiGeneratorService {

    private final Map<String, EntityDefinition> registry = new ConcurrentHashMap<>();
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final DynamicQueryExecutor queryExecutor;

    public void register(EntityDefinition def) {
        registry.put(def.getName().toLowerCase(), def);
    }

    public Optional<EntityDefinition> get(String name) {
        var r = registry.get(name.toLowerCase());
        if (r != null) return Optional.of(r);
        return entityDefinitionRepository.findAll().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Build a parametrized SELECT SQL for the entity based on simple filters.
     * Returns SQL and ordered params.
     */
    public Query buildSelectForEntity(EntityDefinition def, Map<String, Object> eqFilters, Map<String, Object> jsonbFilters) {
        // Use entity name as table by default; schema qualification and tenant schema should be applied by caller.
        String table = def.getName().toLowerCase();
        return DynamicQueryBuilder.buildSelect(table, eqFilters, jsonbFilters);
    }

    /**
     * Execute a simple select for the given entity definition (no filters).
     */
    public List<Map<String, Object>> listAll(EntityDefinition def) {
        var q = buildSelectForEntity(def, Map.of(), Map.of());
        return queryExecutor.execute(q);
    }

    /**
     * List registered entity names.
     */
    public List<String> listRegistered() {
        return registry.keySet().stream().sorted().collect(Collectors.toList());
    }
}
