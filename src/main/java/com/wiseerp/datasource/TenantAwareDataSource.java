package com.wiseerp.datasource;

import com.wiseerp.context.TenantContext;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Logger;

public class TenantAwareDataSource implements DataSource {
    private final DataSource delegate;

    public TenantAwareDataSource(DataSource delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    private void prepareConnection(Connection conn) throws SQLException {
        String tenantId = TenantContext.getTenantId();
        String schema;
        if (tenantId == null || tenantId.isBlank()) {
            schema = "public";
        } else {
            // Follow schema naming convention: tenant_{tenantId}
            // sanitize tenant id to avoid invalid schema names
            schema = "tenant_" + tenantId.replace('-', '_');
        }
        try (Statement s = conn.createStatement()) {
            s.execute("SET search_path TO " + schema + ", public");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = delegate.getConnection();
        prepareConnection(c);
        return c;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection c = delegate.getConnection(username, password);
        prepareConnection(c);
        return c;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(delegate)) {
            return iface.cast(delegate);
        }
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(delegate) || delegate.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        try {
            return delegate.getParentLogger();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
