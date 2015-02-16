package io.progix.dropwizard.jooq;

import io.progix.dropwizard.jooq.tenancy.MultiTenantConnectionProvider;
import io.progix.dropwizard.jooq.tenancy.NoTenantProvider;
import io.progix.dropwizard.jooq.tenancy.TenantProcessingException;
import io.progix.dropwizard.jooq.tenancy.TenantProvider;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    public static final String CONNECTION_PROVIDER_PROPERTY = "jooqConnectionProvider";

    private final Configuration baseConfiguration;
    private final JooqConfiguration annotation;
    private final DataSource dataSource;
    private final MultiTenantConnectionProvider multiTenantConnectionProvider;

    public ConfigurationFactory(Configuration baseConfiguration, DataSource dataSource,
            MultiTenantConnectionProvider multiTenantConnectionProvider, JooqConfiguration annotation) {
        this.baseConfiguration = baseConfiguration;
        this.annotation = annotation;
        this.dataSource = dataSource;
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
    }

    @Override
    public Configuration provide() {
        Configuration configuration = baseConfiguration.derive();

        Connection connection;
        if (annotation.tenantProvider() != NoTenantProvider.class) {
            if (multiTenantConnectionProvider == null) {
                throw new TenantProcessingException(
                        "Can not use multi-tenancy without providing a multiTenantConnectionProvider.");
            }
            try {
                TenantProvider tenantProvider = annotation.tenantProvider().newInstance();
                String tenantIdentifier = tenantProvider.getTenantIdentifier(getContainerRequest());

                configuration.settings().withRenderMapping(new RenderMapping().withSchemata(
                        new MappedSchema().withInput(tenantProvider.inputSchema()).withOutput(tenantIdentifier)));

                connection = multiTenantConnectionProvider.acquire(tenantIdentifier);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new TenantProcessingException(
                        "There was an error instantiating a tenant provider. Make sure you define a no args " +
                                "constructor.",
                        e);
            }
        } else {
            try {
                connection = dataSource.getConnection();
            } catch (SQLException e) {
                throw new DataAccessException("An error occurred while getting a connection.", e);
            }
        }

        if (annotation.transactional()) {
            ConnectionProvider connectionProvider = new DefaultConnectionProvider(connection);
            getContainerRequest().setProperty(CONNECTION_PROVIDER_PROPERTY, connectionProvider);

            configuration.set(connectionProvider);
        } else {
            configuration = baseConfiguration.derive();
        }

        return configuration;
    }
}