package com.wiseerp.metadata.query;

import com.wiseerp.metadata.utils.JsonbFieldUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minimal dynamic query builder producing parametrized SQL and ordered params list.
 */
public final class DynamicQueryBuilder {

    private DynamicQueryBuilder() {}

    public static class Query {
        public final String sql;
        public final List<Object> params;

        public Query(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    /**
     * Build a SELECT query with simple equality filters and JSONB filters.
     * @param schemaQualifiedTable e.g. "tenant_xxx.accounts" or "system.entity_definitions"
     * @param eqFilters map of column -> value for ordinary columns
     * @param jsonbFilters map of jsonb property -> value (column assumed to be 'custom_fields')
     */
    public static Query buildSelect(String schemaQualifiedTable, Map<String, Object> eqFilters, Map<String, Object> jsonbFilters) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(schemaQualifiedTable);

        List<Object> params = new ArrayList<>();
        List<String> parts = new ArrayList<>();

        if (eqFilters != null) {
            for (Map.Entry<String, Object> e : eqFilters.entrySet()) {
                parts.add(e.getKey() + " = ?");
                params.add(e.getValue());
            }
        }

        if (jsonbFilters != null && !jsonbFilters.isEmpty()) {
            String jf = JsonbFieldUtils.jsonbAnd("custom_fields", jsonbFilters);
            if (!jf.isBlank()) {
                parts.add(jf);
                for (Object v : jsonbFilters.values()) params.add(v);
            }
        }

        if (!parts.isEmpty()) {
            sb.append(" WHERE ").append(String.join(" AND ", parts));
        }

        return new Query(sb.toString(), params);
    }
}
