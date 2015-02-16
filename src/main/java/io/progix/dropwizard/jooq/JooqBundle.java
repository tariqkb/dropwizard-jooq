package io.progix.dropwizard.jooq;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.progix.dropwizard.jooq.tenancy.MultiTenantConnectionProvider;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;

public abstract class JooqBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T>,
        DatabaseConfiguration<T> {

    private Configuration configuration;
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T dwConfiguration, Environment environment) throws Exception {
        final DataSourceFactory dbConfig = getDataSourceFactory(dwConfiguration);
        ManagedDataSource dataSource = dbConfig.build(environment.metrics(), "jooq");

        this.configuration = new DefaultConfiguration();
        this.configuration.set(new DataSourceConnectionProvider(dataSource));
        configure(this.configuration);

        environment.jersey().register(JooqTransactionalApplicationListener.class);
        environment.jersey().register(
                new ConfigurationFactoryProvider.Binder(this.configuration, dataSource, multiTenantConnectionProvider));

        environment.lifecycle().manage(dataSource);

        if (multiTenantConnectionProvider != null) {
            environment.lifecycle().manage(multiTenantConnectionProvider);
        }

        environment.healthChecks().register("jooq", new JooqHealthCheck(
                DSL.using(this.configuration.derive(new DefaultConnectionProvider(dataSource.getConnection()))),
                dbConfig.getValidationQuery()));
    }

    public Configuration getConfiguration() {
        return configuration.derive();
    }

    public JooqBundle<T> setMultiTenantConnectionProvider(MultiTenantConnectionProvider multiTenantConnectionProvider) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        return this;
    }

    protected abstract void configure(Configuration configuration);

}
