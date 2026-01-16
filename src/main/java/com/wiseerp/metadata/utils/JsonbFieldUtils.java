package com.wiseerp.metadata.utils;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Lightweight helpers for building JSONB-related SQL fragments.
 */
public final class JsonbFieldUtils {

    private JsonbFieldUtils() {}

    /**
     * Builds a SQL fragment for extracting a JSONB property using ->> operator and adds a placeholder.
     * Example output: "(data->> 'field') = ?"
     */
    public static String jsonbEquals(String column, String path) {
        return String.format("(%s ->> '%s') = ?", column, path);
    }

    /**
     * Builds SQL fragments for multiple jsonb filters joined with AND.
     */
    public static String jsonbAnd(String column, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return "";
        StringJoiner sj = new StringJoiner(" AND ");
        for (String k : filters.keySet()) {
            sj.add(jsonbEquals(column, k));
        }
        return sj.toString();
    }
}
