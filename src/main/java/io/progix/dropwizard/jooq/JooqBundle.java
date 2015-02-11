package io.progix.dropwizard.jooq;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jooq.Configuration;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;

public abstract class JooqBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {

    private Configuration configuration;

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

        environment.jersey().register(new UnitOfJooqApplicationListener(dataSource));
        environment.jersey().register(new ConfigurationFactoryProvider.Binder(this.configuration));

        environment.lifecycle().manage(dataSource);

        environment.healthChecks().register("jooq", new JooqHealthCheck(this.configuration, dbConfig.getValidationQuery()));
    }

    public Configuration getConfiguration() {
        return configuration.derive();
    }

    protected abstract void configure(Configuration configuration);

}
