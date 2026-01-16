package com.wiseerp.metadata.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamicQueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> execute(DynamicQueryBuilder.Query q) {
        if (q == null) return List.of();
        if (q.params == null || q.params.isEmpty()) {
            return jdbcTemplate.queryForList(q.sql);
        }
        return jdbcTemplate.queryForList(q.sql, q.params.toArray());
    }
}
