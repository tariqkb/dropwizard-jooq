package io.progix.dropwizard.jooq;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.progix.dropwizard.jooq.tenancy.MultiTenantConnectionProvider;
import org.jooq.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TestMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private final Map<String, ManagedDataSource> dataSources;
    private final DataSourceFactory dataSourceFactory;
    private final MetricRegistry metricRegistry;
    private final String url;

    public TestMultiTenantConnectionProvider(DataSourceFactory dataSourceFactory, MetricRegistry metricRegistry,
            String url) {
        this.dataSources = new HashMap<>();
        this.dataSourceFactory = dataSourceFactory;
        this.metricRegistry = metricRegistry;
        this.url = url;
    }

    @Override
    public Connection acquire(String tenantIdentifier) {
        ManagedDataSource dataSource = dataSources.get(tenantIdentifier);
        if (dataSource == null) {
            dataSourceFactory.setUrl(url);
            dataSource = dataSourceFactory.build(metricRegistry, "dataSource-" + tenantIdentifier);
            dataSources.put(tenantIdentifier, dataSource);
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("An error occurred while getting a connection.", e);
        }
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        for (ManagedDataSource managedDataSource : dataSources.values()) {
            managedDataSource.stop();
        }
    }
}
