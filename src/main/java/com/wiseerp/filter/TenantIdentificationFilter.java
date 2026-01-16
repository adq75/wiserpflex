package com.wiseerp.filter;

import com.wiseerp.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantIdentificationFilter extends HttpFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            String tenantId = resolveTenant(req);
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(tenantId);
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        // Primary: Header
        // The application expects the `X-Tenant-ID` header to identify tenant schema.
        // Acceptable values:
        //  - "tenant_{uuid}" (full schema name)
        //  - raw UUID string (controller paths accept UUIDs and code prefixes with "tenant_")
        String tenant = request.getHeader(TENANT_HEADER);
        if (tenant != null && !tenant.isBlank()) return tenant.trim();
        // Fallback: path variable or query param
        tenant = request.getParameter("tenantId");
        if (tenant != null && !tenant.isBlank()) return tenant.trim();
        return null;
    }
}
