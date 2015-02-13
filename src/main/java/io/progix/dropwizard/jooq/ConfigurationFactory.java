package io.progix.dropwizard.jooq;

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

import java.sql.SQLException;

public class ConfigurationFactory extends AbstractContainerRequestValueFactory<Configuration> {

    public static final String CONNECTION_PROVIDER_PROPERTY = "jooqConnectionProvider";

    private final Configuration baseConfiguration;
    private final JooqConfiguration annotation;

    public ConfigurationFactory(Configuration baseConfiguration, JooqConfiguration annotation) {
        this.baseConfiguration = baseConfiguration;
        this.annotation = annotation;
    }

    @Override
    public Configuration provide() {
        Configuration configuration;
        if (annotation.transactional()) {
            ConnectionProvider provider = new DefaultConnectionProvider(baseConfiguration.connectionProvider().acquire());
            getContainerRequest().setProperty(CONNECTION_PROVIDER_PROPERTY, provider);
            configuration = baseConfiguration.derive(provider);
        } else {
            configuration = baseConfiguration.derive();
        }

        if (annotation.tenantProvider() != NoTenantProvider.class) {
            try {
                setTenant(annotation.tenantProvider().newInstance(), configuration);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new TenantProcessingException("There was an error instantiating a tenant provider. Make sure you define a no args constructor.", e);
            }
        }

        return configuration;
    }

    private void setTenant(TenantProvider tenantProvider, Configuration configuration) {
        String tenantIdentifier = tenantProvider.getTenantIdentifier(getContainerRequest());

        try {
            ((DefaultConnectionProvider) configuration.connectionProvider()).acquire().setCatalog(tenantIdentifier);
        } catch (SQLException e) {
            throw new DataAccessException("An exception occurred while setting tenant catalog on a connection", e);
        }
        configuration.settings().withRenderMapping(new RenderMapping().withSchemata(new MappedSchema().
                withInput(tenantProvider.inputSchema()).
                withOutput(tenantIdentifier)));
    }
}